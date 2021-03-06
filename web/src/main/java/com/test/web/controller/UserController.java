package com.test.web.controller;

import com.test.mysql.entity.Department;
import com.test.mysql.entity.Role;
import com.test.mysql.entity.User;
import com.test.mysql.entity.Userlog;
import com.test.mysql.model.UserQo;
import com.test.mysql.repository.DepartmentRepository;
import com.test.mysql.repository.RoleRepository;
import com.test.mysql.repository.UserLogRepository;
import com.test.mysql.repository.UserRepository;
import com.test.web.Utils.LogType;
import com.test.web.Utils.ResultType;
import com.test.web.Utils.Test;
import com.test.web.config.CustomSecurityMetadataSource;
import com.test.web.service.security.RoleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.*;

import static java.lang.System.out;

@Controller
@RequestMapping("/user")
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserLogRepository userLogRepository;

    @Autowired
    private RoleManager roleManager;
    @RequestMapping("/index")
    public String index(Model model, Principal user) throws Exception {
        roleManager.giveAuthority(model, user);
        return "user/index";
    }

    @RequestMapping(value = "/{id}")
    public String show(ModelMap model, @PathVariable Long id) {
        User user = userRepository.findOne(id);
        model.addAttribute("user", user);
        return "user/show";
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public Page<User> getList(UserQo userQo) {
        try {
            Pageable pageable = new PageRequest(userQo.getPage(), userQo.getSize(), new Sort(Sort.Direction.ASC, "id"));
            return userRepository.findByName(userQo.getName() == null ? "" : userQo.getName(), pageable);
        } catch (Exception e) {
            logger.error("查询用户失败", e);
        }
        return null;
    }

    @RequestMapping("/new")
    public String create(ModelMap model, User user) {
        List<Department> departments = departmentRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        model.addAttribute("departments", departments);
        model.addAttribute("roles", roles);
        model.addAttribute("user", user);

        return "user/new";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(User user) throws Exception {
        user.setCreatedate(new Date());
        BCryptPasswordEncoder bpe = new BCryptPasswordEncoder();
        user.setPassword(bpe.encode(user.getPassword()));
        userRepository.save(user);
        logger.info("新增->ID=" + user.getId());
        userLogRepository.save(new Userlog(user.getName(),
                new Date(), LogType.ADD_USER, ResultType.SUCCESS
        ));
        return "1";
    }

    @RequestMapping(value = "/edit/{id}")
    public String update(ModelMap model, @PathVariable Long id) {
        User user = userRepository.findOne(id);

        List<Department> departments = departmentRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        List<Long> rids = new ArrayList<Long>();
        for (Role role : user.getRoles()) {
            rids.add(role.getId());
        }

        model.addAttribute("user", user);
        model.addAttribute("departments", departments);
        model.addAttribute("roles", roles);
        model.addAttribute("rids", rids);
        return "user/edit";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update")
    @ResponseBody
    public String update(User user) throws Exception {
        userRepository.save(user);
        logger.info("修改->ID=" + user.getId());
        userLogRepository.save(new Userlog(user.getName(),
                new Date(), LogType.UPDATE_USER, ResultType.SUCCESS
        ));
        return "1";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String delete(@PathVariable Long id, Principal user) throws Exception {
        userRepository.delete(id);
        logger.info("删除->ID=" + id);
        userLogRepository.save(new Userlog(user.getName(),
                new Date(), LogType.DELETE_USER, ResultType.SUCCESS
        ));
        return "1";
    }

}
