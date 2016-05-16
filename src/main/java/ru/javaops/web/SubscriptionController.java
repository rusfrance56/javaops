package ru.javaops.web;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import ru.javaops.model.RegisterType;
import ru.javaops.model.User;
import ru.javaops.model.UserGroup;
import ru.javaops.service.*;
import ru.javaops.service.GroupService.ProjectProps;
import ru.javaops.to.UserTo;
import ru.javaops.to.UserToExt;
import ru.javaops.util.Util;

import javax.mail.MessagingException;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * GKislin
 */
@Controller
public class SubscriptionController {

    @Autowired
    private IntegrationService integrationService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @RequestMapping(value = "/activate", method = RequestMethod.GET)
    public ModelAndView activate(@RequestParam("email") String email, @RequestParam("activate") boolean activate, @RequestParam("key") String key) {
        User u = userService.findExistedByEmail(email);
        if (u.isActive() != activate) {
            u.setActive(activate);
            u.setActivatedDate(new Date());
            userService.save(u);
        }
        return new ModelAndView("activation",
                ImmutableMap.of("activate", activate,
                        "subscriptionUrl", subscriptionService.getSubscriptionUrl(email, key, !activate)));
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView registerByProject(@RequestParam("project") String project,
                                          @RequestParam("channel") String channel,
                                          @Valid UserTo userTo, BindingResult result) throws MessagingException {
        if (result.hasErrors()) {
            throw new ValidationException(Util.getErrorMessage(result));
        }
        return register(channel, "http://javawebinar.ru/confirm.html", "http://javawebinar.ru/error.html", userTo, groupService.getProjectProps(project));
    }

    private ModelAndView register(String channel, String successUrl, String failUrl,
                                  UserTo userTo, ProjectProps projectProps) throws MessagingException {

        UserGroup userGroup = groupService.registerAtProject(userTo, projectProps, channel);
        String projectName = projectProps.project.getName();
        String template = projectName + (userGroup.getRegisterType() == RegisterType.REPEAT ? "_repeat" : "_register");
        String mailResult = mailService.sendToUser(template, userGroup.getUser());
        if (MailService.isOk(mailResult) && userGroup.getRegisterType() == RegisterType.REPEAT){
            integrationService.asyncSendSlackInvitation(userGroup.getUser().getEmail());
        }
        return new ModelAndView("redirectToUrl", "redirectUrl", MailService.isOk(mailResult) ? successUrl : failUrl);
    }

    @RequestMapping(value = "/participate", method = RequestMethod.GET)
    public ModelAndView participate(@RequestParam("email") String email, @RequestParam("key") String key, @RequestParam("project") String project) {
        email = email.toLowerCase();
        ProjectProps projectProps = groupService.getProjectProps(project);
        User u = userService.findByEmailAndGroupId(email, projectProps.currentGroup.getId());
        checkNotNull(u, "Пользователь %s не найден в проекте %s", email, project);
        return new ModelAndView("participation", ImmutableMap.of("user", u, "project", projectProps.project, "key", key));
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ModelAndView save(@RequestParam("key") String key, @Valid UserToExt userToExt, BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(Util.getErrorMessage(result));
        }
        userService.update(userToExt);
        integrationService.asyncSendSlackInvitation(userToExt.getEmail());
        return new ModelAndView("message", "message", "Спасибо за регистрацию.<br/>Проверь почту: должно прийти приглашение в Slack.");
    }
}