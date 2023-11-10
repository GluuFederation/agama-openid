package io.jans.inbound;

import io.jans.as.common.model.common.User;
import io.jans.as.common.service.common.UserService;
import io.jans.service.cdi.util.CdiUtil;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserOnboardHelper {

    private static Logger logger = LoggerFactory.getLogger(UserOnboardHelper.class);
        
    public static String exec(String prefix, Map<String, Object> profile) {
        
        String uid = prefix + profile.get("sub").toString();
        
        //if not existing, insert user with the uid just built
        UserService userService = CdiUtil.bean(UserService.class);
        logger.debug("Retrieving user identified by {}", uid);
        User user = userService.getUser(uid);
        
        if (user != null) {
            logger.debug("Found!");
        } else {
            logger.debug("Not found. Inserting entry");
            user = new User();
            user.setUserId(uid);
            userService.addUser(user, true);
        }
        return uid;
        
    }

}
