package starter.security;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import starter.rest.Json;
import starter.service.Constant;
import starter.service.UserService;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class EsUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Json json = userService.get(username, false);
        if (json!=null){
            User user = new User(username, json.get(Constant.FieldName.PASSWORD).toString(), new ArrayList<>());
            return user;
        }else{
            throw new UsernameNotFoundException("Not found");
        }
    }
}
