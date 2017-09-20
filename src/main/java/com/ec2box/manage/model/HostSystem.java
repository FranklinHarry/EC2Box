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
package com.ec2box.manage.model;

import com.ec2box.common.util.AppConfig;

import java.util.List;

/**
 * Value object that contains host system information
 */
public class HostSystem {


    public static final String INITIAL_STATUS = "INITIAL";
    public static final String AUTH_FAIL_STATUS = "AUTHFAIL";
    public static final String PUBLIC_KEY_FAIL_STATUS = "KEYAUTHFAIL";
    public static final String GENERIC_FAIL_STATUS = "GENERICFAIL";
    public static final String SUCCESS_STATUS = "SUCCESS";
    public static final String HOST_FAIL_STATUS="HOSTFAIL";

    Long id;
    String displayNm = "";
    String instance;
    String user = AppConfig.getProperty("defaultSystemUser");
    String host;
    Integer port = Integer.parseInt(AppConfig.getProperty("defaultSystemPort"));
    Long keyId;
    String displayLabel;
    String ec2Region;
    String state;
    boolean checked = false;
    Integer instanceId;

    String statusCd = INITIAL_STATUS;
    String errorMsg;
    String systemStatus;
    String instanceStatus;
    Integer monitorAlarm=0;
    Integer monitorInsufficientData=0;
    Integer monitorOk=0;


    List<String> publicKeyList;

    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayNm() {
        return displayNm;
    }

    public List<String> getPublicKeyList() {
        return publicKeyList;
    }

    public void setPublicKeyList(List<String> publicKeyList) {
        this.publicKeyList = publicKeyList;
    }

    public void setDisplayNm(String displayNm) {
        this.displayNm = displayNm;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public String getDisplayLabel() {
        if (this.displayNm != null && !this.displayNm.trim().equals(""))
            return getDisplayNm() + " - ( " + getHost() + " )";
        else {
            return getHost();
        }
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getEc2Region() {
        return ec2Region;
    }

    public void setEc2Region(String ec2Region) {
        this.ec2Region = ec2Region;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public String getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(String instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    public Integer getMonitorOk() {
        return monitorOk;
    }

    public void setMonitorOk(Integer monitorOk) {
        this.monitorOk = monitorOk;
    }

    public Integer getMonitorAlarm() {
        return monitorAlarm;
    }

    public void setMonitorAlarm(Integer monitorAlarm) {
        this.monitorAlarm = monitorAlarm;
    }

    public Integer getMonitorInsufficientData() {
        return monitorInsufficientData;
    }

    public void setMonitorInsufficientData(Integer monitorInsufficientData) {
        this.monitorInsufficientData = monitorInsufficientData;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }
}
