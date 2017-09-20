/**
 * Copyright 2015 Sean Kavanagh - sean.p.kavanagh6@gmail.com
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

import com.ec2box.manage.db.ProfileDB;
import com.ec2box.manage.db.UserDB;
import com.ec2box.manage.db.UserProfileDB;
import com.ec2box.manage.model.Profile;
import com.ec2box.manage.model.SortedSet;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import java.util.List;

/**
 * Action to assign users to profiles
 */
@InterceptorRef("ec2boxStack")
public class ProfileUsersAction extends ActionSupport {

    Profile profile;
    SortedSet sortedSet=new SortedSet();
    List<Long> userSelectId;


    @Action(value = "/manage/viewProfileUsers",
            results = {
                    @Result(name = "success", location = "/manage/view_profile_users.jsp")
            }
    )
    public String viewProfileUsers() {
        if (profile != null && profile.getId() != null) {
            profile = ProfileDB.getProfile(profile.getId());
            sortedSet = UserDB.getAdminUserSet(sortedSet, profile.getId());
        }
        return SUCCESS;
    }

    @Action(value = "/manage/assignUsersToProfile",
            results = {
                    @Result(name = "success", location = "/manage/viewProfiles.action", type = "redirect")
            }
    )
    public String assignSystemsToProfile() {

        if (userSelectId!=null) {
            UserProfileDB.setUsersForProfile(profile.getId(), userSelectId);
        }
        return SUCCESS;
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Long> getUserSelectId() {
        return userSelectId;
    }

    public void setUserSelectId(List<Long> userSelectId) {
        this.userSelectId = userSelectId;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public SortedSet getSortedSet() {
        return sortedSet;
    }

}
