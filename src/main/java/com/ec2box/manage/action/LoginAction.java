/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ec2box.manage.action;

import com.ec2box.common.util.AppConfig;
import com.ec2box.common.util.AuthUtil;
import com.ec2box.manage.db.AuthDB;
import com.ec2box.manage.model.Auth;
import com.ec2box.manage.util.OTPUtil;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.InterceptorRefs;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;


/**
 * Action to login to ec2box
 */
@InterceptorRef("ec2boxStack")
public class LoginAction extends ActionSupport implements ServletRequestAware {

    HttpServletRequest servletRequest;
    private static Logger loginAuditLogger = LoggerFactory.getLogger("com.ec2box.manage.action.LoginAudit");
    Auth auth;
    //check if otp is enabled
    boolean otpEnabled="true".equals(AppConfig.getProperty("enableOTP"));
    private final String AUTH_ERROR="Authentication Failed : Login credentials are invalid";
    private final String AUTH_SUCCESS="Authentication Successful";


    @Action(value = "/login",
            results = {
                    @Result(name = "success", location = "/login.jsp")
            }
    )
    public String login() {

        return SUCCESS;
    }

    @Action(value = "/admin/menu",
            results = {
                    @Result(name = "success", location = "/admin/menu.jsp")
            }
    )
    public String menu() {

        return SUCCESS;
    }


    @Action(value = "/loginSubmit",
            results = {
                    @Result(name = "input", location = "/login.jsp"),
                    @Result(name = "change_password", location = "/admin/userSettings.action", type = "redirect"),
                    @Result(name = "otp", location = "/admin/viewOTP.action", type = "redirect"),
                    @Result(name = "success", location = "/admin/menu.action", type = "redirect")
            }
    )
    public String loginSubmit() {
        String retVal = SUCCESS;
        //get client IP
        String clientIP = null;
        if (StringUtils.isNotEmpty(AppConfig.getProperty("clientIPHeader"))) {
            clientIP = servletRequest.getHeader(AppConfig.getProperty("clientIPHeader"));
        }
        if (StringUtils.isEmpty(clientIP)) {
            clientIP = servletRequest.getRemoteAddr();
        }

        String authToken = AuthDB.loginAdmin(auth);
        if (authToken != null) {
            Long userId = AuthDB.getUserIdByAuthToken(authToken);
            String sharedSecret = null;
            if (otpEnabled) {
                sharedSecret = AuthDB.getSharedSecret(userId);
                if (StringUtils.isNotEmpty(sharedSecret) && (auth.getOtpToken() == null || !OTPUtil.verifyToken(sharedSecret, auth.getOtpToken()))) {
                    loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - "  + AUTH_ERROR);
                    addActionError(AUTH_ERROR);
                    return INPUT;
                }
            }

            AuthUtil.setAuthToken(servletRequest.getSession(), authToken);
            AuthUtil.setUserId(servletRequest.getSession(), AuthDB.getUserIdByAuthToken(authToken));
            AuthUtil.setTimeout(servletRequest.getSession());
            loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - "  + AUTH_SUCCESS);

            //for first time login redirect to set OTP
            if (otpEnabled && StringUtils.isEmpty(sharedSecret)) {
                return "otp";
            }
            else if ("changeme".equals(auth.getPassword())) {
                retVal = "change_password";
            }

        } else {
            loginAuditLogger.info(auth.getUsername() + " (" + clientIP + ") - "  + AUTH_ERROR);
            addActionError(AUTH_ERROR);
            retVal = INPUT;
        }


        return retVal;
    }

    @Action(value = "/logout",
            results = {
                    @Result(name = "success", location = "/login.action", type = "redirect")
            }
    )
    public String logout() {
        AuthUtil.deleteAllSession(servletRequest.getSession());
        return SUCCESS;
    }



    /**
     * Validates fields for login submit
     */
    public void validateLoginSubmit() {
        if (auth.getUsername() == null ||
                auth.getUsername().trim().equals("")) {
            addFieldError("auth.username", "Required");
        }
        if (auth.getPassword() == null ||
                auth.getPassword().trim().equals("")) {
            addFieldError("auth.password", "Required");
        }


    }


    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    public boolean isOtpEnabled() {
        return otpEnabled;
    }

    public void setOtpEnabled(boolean otpEnabled) {
        this.otpEnabled = otpEnabled;
    }
}
