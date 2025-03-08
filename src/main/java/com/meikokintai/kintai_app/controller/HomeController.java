package com.meikokintai.kintai_app.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.meikokintai.kintai_app.model.IncomeTax;
import com.meikokintai.kintai_app.model.Manager;
import com.meikokintai.kintai_app.model.Salary;
import com.meikokintai.kintai_app.model.User;
import com.meikokintai.kintai_app.model.Work;
import com.meikokintai.kintai_app.model.WorkTemplate;
import com.meikokintai.kintai_app.model.Lock;
import com.meikokintai.kintai_app.service.IncomeTaxService;
import com.meikokintai.kintai_app.service.ManagerService;
import com.meikokintai.kintai_app.service.SalaryService;
import com.meikokintai.kintai_app.service.UserService;
import com.meikokintai.kintai_app.service.WorkService;
import com.meikokintai.kintai_app.service.WorkTemplateService;
import com.meikokintai.kintai_app.service.LockService;
import com.meikokintai.kintai_app.util.DateSet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
    
    // サービスの宣言
    private final WorkService workService;
    private final UserService userService;
    private final ManagerService managerService;
    private final SalaryService salaryService;
    private final WorkTemplateService workTemplateService;
    private final IncomeTaxService incomeTaxService;
    private final LockService lockService;

    // ドメイン名
    private final String domainLocal = "localhost:8080"; // ローカル環境
    private final String domainAWS = "meikokintai.com"; // 本番環境
    
    // ホームコントローラー
    public HomeController(WorkService workService, UserService userService, ManagerService managerService, SalaryService salaryService, WorkTemplateService workTemplateService, IncomeTaxService incomeTaxService, LockService lockService) {
        this.workService = workService;
        this.userService = userService;
        this.managerService = managerService;
        this.salaryService = salaryService;
        this.workTemplateService = workTemplateService;
        this.incomeTaxService = incomeTaxService;
        this.lockService = lockService;
    }
    
    // 講師アカウントでの処理
    /// シフトの登録・修正・削除
    /// テンプレートの登録・修正・削除
    /// 給与情報の確認
    /// アカウント情報の修正

    // ログイン（get）
    @GetMapping("/login")
    String loginGet(HttpServletRequest request, Model model) {
        try {
            User user = new User();
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("userLoginId".equals(cookie.getName())) {
                        user.setLoginId(cookie.getValue());
                    } else if ("userPassword".equals(cookie.getName())) {
                        user.setPassword(cookie.getValue());
                    }
                }
            }
            model.addAttribute("userLoginForm", user);
            return "login";
        } catch (Exception e) {
            System.out.println("Error happened in login.html");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // ログイン（post）
    @PostMapping("/login")
    String loginPost(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("userLoginForm") User loginUser, RedirectAttributes redirectAttributes) {
        try {
            User trueUser = userService.getByLoginId(loginUser.getLoginId());
            if (trueUser == null || !trueUser.getPassword().equals(loginUser.getPassword())) {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:login";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                    return redirectUrl;
                }
            } else {
                Cookie cookieLoginId = new Cookie("userLoginId", trueUser.getLoginId());
                cookieLoginId.setMaxAge(30 * 24 * 60 * 60);
                cookieLoginId.setHttpOnly(true);
                cookieLoginId.setPath("/");
                response.addCookie(cookieLoginId);
                Cookie cookiePassword = new Cookie("userPassword", trueUser.getPassword());
                cookiePassword.setMaxAge(30 * 24 * 60 * 60);
                cookiePassword.setHttpOnly(true);
                cookiePassword.setPath("/");
                response.addCookie(cookiePassword);
                UUID userId = trueUser.getId();                        
                redirectAttributes.addAttribute("user", userId);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:index";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/index", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            System.out.println("Error happened in login(post)");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // ホーム
    @GetMapping("/index")
    String index(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "index";
        } catch (Exception e) {
            System.out.println("Error happened in index.html");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // シフト管理
    @GetMapping("/info")
    String info(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            Date dateFrom = DateSet.getDatePeriod(yearNow, monthNow)[0];
            Date dateTo = DateSet.getDatePeriod(yearNow, monthNow)[1];
            String yearBefore = DateSet.getDateBefore(yearNow, monthNow)[0];
            String monthBefore = DateSet.getDateBefore(yearNow, monthNow)[1];
            String yearNext = DateSet.getDateNext(yearNow, monthNow)[0];
            String monthNext = DateSet.getDateNext(yearNow, monthNow)[1];
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            Map<UUID, String> supportSalaryMap = new HashMap<>();
            Map<UUID, String> carfareMap = new HashMap<>();
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            Lock lock = lockService.getByTarget(user.getClassAreaId(), user.getId(), Integer.parseInt(yearNow), Integer.parseInt(monthNow));
            Boolean lockStatus;
            if (lock == null || !lock.getStatus()) {
                lockStatus = false;
            } else {
                lockStatus = true;
            }
            for (Work work : workList) {
                supportSalaryMap.put(work.getId(), formatter.format(salaryService.getByDate(UUID.fromString(userId), work.getDate()).getSupportSalary()));
                carfareMap.put(work.getId(), formatter.format(work.getCarfare()));
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("workList", workList);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            model.addAttribute("lockStatus", lockStatus);
            model.addAttribute("supportSalaryMap", supportSalaryMap);
            model.addAttribute("carfareMap", carfareMap);
            redirectAttributes.addAttribute("user", userId);
            return "info";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > シフト登録（get）
    @GetMapping("/addForm")
    String addFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(yearNow, monthNow)[0];
            String monthBefore = DateSet.getDateBefore(yearNow, monthNow)[1];
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Work work = new Work();
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            work.setUserId(user.getId());
            work.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("workCreateForm", work);
            model.addAttribute("templateList", templateList);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            redirectAttributes.addAttribute("user", userId);
            return "addForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > シフト登録（post）
    @PostMapping("/addForm")
    String addFormPost(HttpServletRequest request, @ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            Lock lock = lockService.getByTarget(user.getClassAreaId(), user.getId(), Integer.parseInt(year), Integer.parseInt(month));
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                if (lock == null || !lock.getStatus()) {
                    form = workService.calcTimeAndSalary(form);
                    workService.add(form);
                    String host = request.getHeader("Host");
                    if (host.equals(domainLocal)) {
                        return "redirect:info";
                    } else {
                        String redirectUrl = String.format("redirect:https://%s/info", domainAWS);
                        return redirectUrl;
                    }
                } else {
                    String host = request.getHeader("Host");
                    if (host.equals(domainLocal)) {
                        return "redirect:addForm";
                    } else {
                        String redirectUrl = String.format("redirect:https://%s/addForm", domainAWS);
                        return redirectUrl;
                    }
                }
            } catch (Exception e) {
                redirectAttributes.addAttribute("year", yearNow);
                redirectAttributes.addAttribute("month", monthNow);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:addForm";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/addForm", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > シフト修正（get）
    @GetMapping("/editForm")
    String editFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("detail") String detailId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            Work work = workService.findWorkById(UUID.fromString(detailId));
            Lock lock = lockService.getByTarget(user.getClassAreaId(), user.getId(), Integer.parseInt(yearNow), Integer.parseInt(monthNow));
            if (lock == null || !lock.getStatus()) {
                if (work.getTimeStart().equals("     ")) {
                    work.setTimeStart("");
                }
                if (work.getTimeEnd().equals("     ")) {
                    work.setTimeEnd("");
                }
                if (work.getOfficeTimeStart().equals("     ")) {
                    work.setOfficeTimeStart("");
                }
                if (work.getOfficeTimeEnd().equals("     ")) {
                    work.setOfficeTimeEnd("");
                }
                if (work.getOtherTimeStart().equals("     ")) {
                    work.setOtherTimeStart("");
                }
                if (work.getOtherTimeEnd().equals("     ")) {
                    work.setOtherTimeEnd("");
                }
                List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
                model.addAttribute("user", user);
                model.addAttribute("manager", manager);
                model.addAttribute("year", year);
                model.addAttribute("month", month);
                model.addAttribute("workUpdateForm", work);
                model.addAttribute("templateList", templateList);
                model.addAttribute("yearNow", yearNow);
                model.addAttribute("monthNow", monthNow);
                redirectAttributes.addAttribute("user", userId);
                return "editForm";
            } else {
                redirectAttributes.addAttribute("user", user.getId());
                redirectAttributes.addAttribute("year", year);
                redirectAttributes.addAttribute("month", month);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:info";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/info", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > シフト修正（post）
    @PostMapping("/editForm")
    String editFormPost(HttpServletRequest request, @ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            Lock lock = lockService.getByTarget(user.getClassAreaId(), user.getId(), Integer.parseInt(year), Integer.parseInt(month));
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                if (lock == null || !lock.getStatus()) {
                    form = workService.calcTimeAndSalary(form);
                    workService.update(form);
                }
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:info";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/info", domainAWS);
                    return redirectUrl;
                }
            } catch (Exception e) {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:editForm";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/editForm", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // シフト管理 > 削除
    @PostMapping("/deleteWork")
    String deleteWork(HttpServletRequest request, @Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Work work = workService.findWorkById(UUID.fromString(deleteId));
            String year = DateSet.getYear(work.getDate());
            String month = DateSet.getMonth(work.getDate());
            Lock lock = lockService.getByTarget(user.getClassAreaId(), user.getId(), Integer.parseInt(year), Integer.parseInt(month));
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            if (lock == null || !lock.getStatus()) {
                workService.deleteById(UUID.fromString(deleteId));
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:info";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/info", domainAWS);
                    return redirectUrl;
                }
            } else {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:info";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/info", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // シフト管理 > テンプレート
    @GetMapping("/infoTemplate")
    String infoTemplate(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            Map<UUID, String> carfareMap = new HashMap<>();
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            for (WorkTemplate template : templateList) {
                carfareMap.put(template.getId(), formatter.format(template.getCarfare()));
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("templateList", templateList);
            model.addAttribute("carfareMap", carfareMap);
            redirectAttributes.addAttribute("user", userId);
            return "infoTemplate";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // シフト管理 > テンプレート > 登録（get）
    @GetMapping("/templateForm")
    String templateFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            WorkTemplate template = new WorkTemplate();
            template.setUserId(user.getId());
            template.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("templateCreateForm", template);
            redirectAttributes.addAttribute("user", userId);
            return "templateForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > テンプレート > 登録（post）
    @PostMapping("/templateForm")
    String templateFormPost(HttpServletRequest request, @ModelAttribute("templateCreateForm") WorkTemplate form, RedirectAttributes redirectAttributes) {
        try {
            workTemplateService.add(form);
            redirectAttributes.addAttribute("user", form.getUserId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoTemplate";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoTemplate", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > テンプレート > 修正（get）
    @GetMapping("/editTemplateForm")
    String editTemplateFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("template") String templateId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            WorkTemplate template = workTemplateService.findTemplateById(UUID.fromString(templateId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("template", template);
            model.addAttribute("templateUpdateForm", template);
            redirectAttributes.addAttribute("user", userId);
            return "editTemplateForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // シフト管理 > テンプレート > 修正（post）
    @PostMapping("/editTemplateForm")
    String editTemplateFormPost(HttpServletRequest request, @ModelAttribute("templateUpdateForm") WorkTemplate form, RedirectAttributes redirectAttributes) {
        try {
            workTemplateService.update(form);
            redirectAttributes.addAttribute("user", form.getUserId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoTemplate";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoTesmplate", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // シフト管理 > テンプレート > 削除
    @PostMapping("/deleteTemplate")
    String deleteTemplate(HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId) {
        try {
            workTemplateService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("user", userId);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoTemplate";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoTemplate", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 給与情報
    @GetMapping("/infoSalary")
    String infoSalary(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow, @RequestParam("tax") String tax) {
        try {
            // 基本情報の取得
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));

            // 給与明細の計算
            Date dateFrom = DateSet.getDatePeriod(yearNow, monthNow)[0];
            Date dateTo = DateSet.getDatePeriod(yearNow, monthNow)[1];
            String yearBefore = DateSet.getDateBefore(yearNow, monthNow)[0];
            String monthBefore = DateSet.getDateBefore(yearNow, monthNow)[1];
            String yearNext = DateSet.getDateNext(yearNow, monthNow)[0];
            String monthNext = DateSet.getDateNext(yearNow, monthNow)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Map<UUID, Salary> salaryMap = new HashMap<>();
            int sumSalaryPre[] = new int[17];
            int sumSalary[] = new int[17];
            String sumSalaryFormatted[] = new String[19];
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            double setDouble[] = new double[16];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            IncomeTax incomeTax;
            int incomeTaxValue = 0;
            String incomeTaxFormatted;
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            try {
                sumSalary = workService.calcSumSalary(UUID.fromString(userId), dateFrom, dateTo, salary.getClassSalary(), salary.getOfficeSalary(), salary.getSupportSalary());
                for (Work work : workList) {
                    setDouble = workService.calcSumSalary(work, salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    salaryMap.put(work.getId(), salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    for (int i = 0; i < 16; i++) {
                        resultDouble[i]+= setDouble[i];
                    }
                }
                for (int i = 0; i < 16; i++) {
                    sumSalaryPre[i] = (int)Math.ceil(resultDouble[i]);
                    if (i != 0 && i != 2 && i != 3 && i != 5 && i != 7 && i != 10 && i != 12 && i != 14) {
                        sumSalaryPre[16] += sumSalaryPre[i];
                    }
                }
                sumSalary[16] += sumSalaryPre[15] - sumSalary[15] + sumSalaryPre[13] - sumSalary[13] + sumSalaryPre[9] - sumSalary[9] + sumSalaryPre[8] - sumSalary[8] + sumSalaryPre[6] - sumSalary[6] + sumSalaryPre[4] - sumSalary[4];
                sumSalary[15] = sumSalaryPre[15];
                sumSalary[13] = sumSalaryPre[13];
                sumSalary[9] = sumSalaryPre[9];
                sumSalary[8] = sumSalaryPre[8];
                sumSalary[6] = sumSalaryPre[6];
                sumSalary[4] = sumSalaryPre[4];
                sumSalary[10] = sumSalaryPre[16] - sumSalary[16];
                sumSalary[16] += sumSalary[10];
            } catch (Exception e) {
                for (int i = 0; i < 17; i++) {
                    sumSalary[i] = 0;
                    sumSalaryPre[i]= 0;
                }
            }
            if (tax.equals("on")) {
                int totalIncome = sumSalary[1] + sumSalary[4] + sumSalary[8] + sumSalary[6] + sumSalary[9] + sumSalary[10] + sumSalary[13]  + sumSalary[15];
                if (totalIncome >= 88000) {
                    if (incomeTaxService.getByTotalIncome(totalIncome) != null) {
                        incomeTax = incomeTaxService.getByTotalIncome(totalIncome);
                        incomeTaxValue = incomeTax.getTax();
                        sumSalary[16] -= incomeTaxValue;
                    }
                }
            }
            incomeTaxFormatted = formatter.format(incomeTaxValue);
            for (int i = 0; i < sumSalaryFormatted.length; i++) {
                if (i == 1 || i == 4 || i == 6 || i == 8 || i == 9 || i == 10 || i == 11 || i == 13 || i == 15 || i == 16) {
                    sumSalaryFormatted[i] = formatter.format(sumSalary[i]);
                } else if (i == 17) {
                    sumSalaryFormatted[i] = formatter.format(salary.getClassSalary());
                } else if (i == 18) {
                    sumSalaryFormatted[i] = formatter.format(sumSalary[4] + sumSalary[8] + sumSalary[13] + sumSalary[15]);
                } else {
                    sumSalaryFormatted[i] = Integer.toString(sumSalary[i]);
                }
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("salary", salary);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("sumSalaryFormatted", sumSalaryFormatted);
            model.addAttribute("salaryMap", salaryMap);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            model.addAttribute("tax", tax);
            model.addAttribute("incomeTaxFormatted", incomeTaxFormatted);
            return "infoSalary";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 給与情報 > 給与推移
    @GetMapping("/detailSalary")
    String detailSalary(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            String today = String.valueOf(calendar.get(Calendar.YEAR)) + "/" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "/" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " 現在";
            Salary salaryNow = salaryService.getByDate(user.getId(), year+"-"+month+"-"+day);
            String salaryFormatted[] = new String[4];
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            Map<UUID, String[]> salaryMapFormatted = new HashMap<>();
            salaryFormatted[0] = String.format("%,d", salaryNow.getClassSalary());
            salaryFormatted[1] = String.format("%,d", salaryNow.getOfficeSalary());
            salaryFormatted[2] = String.format("%,d", salaryNow.getSupportSalary());
            salaryFormatted[3] = String.format("%,d", salaryNow.getCarfare());
            for (Salary salary : salaryList) {
                String salariesFormatted[] = new String[4];
                salariesFormatted[0] = String.format("%,d", salary.getClassSalary());
                salariesFormatted[1] = String.format("%,d", salary.getOfficeSalary());
                salariesFormatted[2] = String.format("%,d", salary.getSupportSalary());
                salariesFormatted[3] = String.format("%,d", salary.getCarfare());
                salaryMapFormatted.put(salary.getId(), salariesFormatted);
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("salaryFormatted", salaryFormatted);
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("salaryMapFormatted", salaryMapFormatted);
            model.addAttribute("today", today);
            return "detailSalary";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // アカウント
    @GetMapping("/user")
    String user(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "user";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // アカウント > アカウント情報修正（get）
    @GetMapping("/userForm")
    String userFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("userUpdateForm", user);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "userForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // アカウント > アカウント情報修正（post）
    @PostMapping("/userForm")
    String userFormPost(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("userUpdateForm") User form, RedirectAttributes redirectAttributes) {
        try {
            userService.update(form);
            Cookie cookieLoginId = new Cookie("userLoginId", form.getLoginId());
            cookieLoginId.setMaxAge(30 * 24 * 60 * 60);
            cookieLoginId.setHttpOnly(true);
            cookieLoginId.setPath("/");
            response.addCookie(cookieLoginId);
            Cookie cookiePassword = new Cookie("userPassword", form.getPassword());
            cookiePassword.setMaxAge(30 * 24 * 60 * 60);
            cookiePassword.setHttpOnly(true);
            cookiePassword.setPath("/");
            response.addCookie(cookiePassword);
            redirectAttributes.addAttribute("user", form.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:user";
            } else {
                String redirectUrl = String.format("redirect:https://%s/user", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:login";
            } else {
                String redirectUrl = String.format("redirect:https://%s/login", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 社員アカウントでの処理
    /// 機能
    //// 管理者アカウントの作成・修正・削除
    //// 講師アカウントの作成・修正・削除
    //// シフト登録・修正・削除
    //// ロック状態の変更
    //// Excelファイルのエクスポート
    //// 給与情報の登録・修正・削除

    // アカウント作成（get）
    @GetMapping("/signUpManager")
    String signUpManagerGet(HttpServletRequest request, Model model) {
        try {
            model.addAttribute("managerCreateForm", new Manager());
            return "signUpManager";
        } catch (Exception e) {
            System.out.println("Error happened in signUpManager.html");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // アカウント作成（post）
    @PostMapping("/signUpManager")
    String signUpManagerPost(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("managerCreateForm") Manager manager) {
        try {
            if (managerService.getByLoginId(manager.getLoginId()) == null) {
                managerService.add(manager.getLoginId(), manager.getPassword(), manager.getClassArea());
                Cookie cookieLoginId = new Cookie("managerLoginId", manager.getLoginId());
                cookieLoginId.setMaxAge(30 * 24 * 60 * 60);
                cookieLoginId.setHttpOnly(true);
                cookieLoginId.setPath("/");
                response.addCookie(cookieLoginId);
                Cookie cookiePassword = new Cookie("managerPassword", manager.getPassword());
                cookiePassword.setMaxAge(30 * 24 * 60 * 60);
                cookiePassword.setHttpOnly(true);
                cookiePassword.setPath("/");
                response.addCookie(cookiePassword);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:loginManager";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                    return redirectUrl;
                }
            } else {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:signUpManager";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/signUpManager", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            System.out.println("Error happened in signUpManager(post)");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // ログイン（get）
    @GetMapping("/loginManager")
    String loginManagerGet(HttpServletRequest request, Model model) {
        try {
            Manager manager = new Manager();
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("managerLoginId".equals(cookie.getName())) {
                        manager.setLoginId(cookie.getValue());
                    } else if ("managerPassword".equals(cookie.getName())) {
                        manager.setPassword(cookie.getValue());
                    }
                }
            }
            model.addAttribute("managerLoginForm", manager);
            return "loginManager";
        } catch (Exception e) {
            System.out.println("Error happened in loginManager.html");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // ログイン（post）
    @PostMapping("/loginManager")
    String loginManagerPost(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("managerLoginForm") Manager loginManager, RedirectAttributes redirectAttributes) {
        try {
            Manager trueManager = managerService.getByLoginId(loginManager.getLoginId());
            if (trueManager == null || !trueManager.getPassword().equals(loginManager.getPassword())) {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:loginManager";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                    return redirectUrl;
                }
            } else {
                Cookie cookieLoginId = new Cookie("managerLoginId", trueManager.getLoginId());
                cookieLoginId.setMaxAge(30 * 24 * 60 * 60);
                cookieLoginId.setHttpOnly(true);
                cookieLoginId.setPath("/");
                response.addCookie(cookieLoginId);
                Cookie cookiePassword = new Cookie("managerPassword", trueManager.getPassword());
                cookiePassword.setMaxAge(30 * 24 * 60 * 60);
                cookiePassword.setHttpOnly(true);
                cookiePassword.setPath("/");
                response.addCookie(cookiePassword);
                UUID managerId = trueManager.getId();
                redirectAttributes.addAttribute("manager", managerId);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:indexManager";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/indexManager", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            System.out.println("Error happened in loginManager(post)");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // ホーム
    @GetMapping("/indexManager")
    String indexManager(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "indexManager";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理
    @GetMapping("/detailClass")
    String detailClass(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(UUID.fromString(managerId));
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "detailClass";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理 > 新規登録（get）
    @GetMapping("/signUp")
    String signUpGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = new User();
            Salary salary = new Salary();
            user.setClassAreaId(manager.getId());
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("userCreateForm", user);
            model.addAttribute("salaryCreateForm", salary);
            return "signUp";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 新規登録（post）
    @PostMapping("/signUp")
    String signUpPost(HttpServletRequest request, @ModelAttribute("userCreateForm") User user, @ModelAttribute("salaryCreateForm") Salary salary, RedirectAttributes redirectAttributes) {
        try {
            if (userService.getByLoginId(user.getLoginId()) == null) {
                user.setState(true);
                user.setRetireDate("");
                userService.add(user);
                User setUser = userService.getByLoginId(user.getLoginId());
                salary.setUserId(setUser.getId());
                salaryService.add(salary);
                redirectAttributes.addAttribute("manager", user.getClassAreaId());
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:detailClass";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/detailClass", domainAWS);
                    return redirectUrl;
                }
            } else {
                redirectAttributes.addAttribute("manager", user.getClassAreaId());
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:signUp";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/signUp", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報
    @GetMapping("/infoUser")
    String infoUser(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String today = String.valueOf(calendar.get(Calendar.YEAR)) + "/" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "/" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " 現在";
            User user = userService.getByUserId(UUID.fromString(userId));
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), year+"-"+month+"-"+day);
            String salaryFormatted[] = new String[4];
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            Map<UUID, String[]> salaryMapFormatted = new HashMap<>();
            String dateStart = salaryService.getFirstSalary(user.getId()).getDateFrom();
            String term;
            if (user.getState()) {
                term = DateSet.getTerm(dateStart);
            } else {
                term = DateSet.getTerm(dateStart, user.getRetireDate());
            }
            for (Salary s : salaryList) {
                String salariesFormatted[] = new String[4];
                salariesFormatted[0] = String.format("%,d", s.getClassSalary());
                salariesFormatted[1] = String.format("%,d", s.getOfficeSalary());
                salariesFormatted[2] = String.format("%,d", s.getSupportSalary());
                salariesFormatted[3] = String.format("%,d", s.getCarfare());
                salaryMapFormatted.put(s.getId(), salariesFormatted);
            }
            salaryFormatted[0] = String.format("%,d", salary.getClassSalary());
            salaryFormatted[1] = String.format("%,d", salary.getOfficeSalary());
            salaryFormatted[2] = String.format("%,d", salary.getSupportSalary());
            salaryFormatted[3] = String.format("%,d", salary.getCarfare());
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("salary", salary);
            model.addAttribute("salaryFormatted", salaryFormatted);
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("salaryMapFormatted", salaryMapFormatted);
            model.addAttribute("term", term);
            model.addAttribute("today", today);
            return "infoUser";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報 > アカウント情報修正（get）
    @GetMapping("editUserForm")
    String editUserFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(UUID.fromString(userId));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("userUpdateForm", user);
            return "editUserForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理 > 基本情報 > アカウント情報修正（post）
    @PostMapping("/editUserForm")
    String editUserFormPost(HttpServletRequest request, @ModelAttribute("userUpdateForm") User user, RedirectAttributes redirectAttributes) {
        try {
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            userService.update(user);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in editUserForm(post)");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理 > 基本情報 > 退職手続（get）
    @GetMapping("/retireForm")
    String retireFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(UUID.fromString(userId));
            user.setState(false);
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("retireCreateForm", user);
            return "retireForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理 > 基本情報 > 退職手続（post）
    @PostMapping("/retireForm")
    String retireFormPost(HttpServletRequest request, RedirectAttributes redirectAttributes, @ModelAttribute("retireCreateForm") User user) {
        try {
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            userService.update(user);
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("user", user.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in retireForm(post)");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報 > 退職取消
    @GetMapping("/resetRetire")
    String deleteRetire(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            User user = userService.getByUserId(UUID.fromString(userId));
            user.setState(true);
            user.setRetireDate("");
            userService.update(user);
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("user", user.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報 > アカウント削除
    @GetMapping("/deleteUser")
    String deleteUser(HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("manager") String managerId) {
        try {
            for (Work work : workService.findAllByUserId(UUID.fromString(userId))) {
                workService.deleteById(work.getId());
            }
            for (Salary salary : salaryService.findByUserId(UUID.fromString(userId))) {
                salaryService.delete(salary);
            }
            for (WorkTemplate template : workTemplateService.findByUserId(UUID.fromString(userId))) {
                workTemplateService.deleteById(template.getId());
            }
            for (Lock lock : lockService.findByUserId(UUID.fromString(userId))) {
                lockService.deleteById(lock);
            }
            userService.deleteById(UUID.fromString(userId));
            redirectAttributes.addAttribute("manager", managerId);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:detailClass";
            } else {
                String redirectUrl = String.format("redirect:https://%s/detailClass", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理 > 基本情報 > 給与更新（get）
    @GetMapping("/updateForm")
    String updateFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(UUID.fromString(userId));
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            salary.setDateFrom("");
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("salaryUpdateForm", salary);
            return "updateForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 講師管理 > 基本情報 > 給与更新（post）
    @PostMapping("/updateForm")
    String updateFormPost(HttpServletRequest request, @ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            salaryService.add(form);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報 > 給与修正（get）
    @GetMapping("/salaryForm")
    String salaryFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("salary") String salaryId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(UUID.fromString(userId));
            Salary salary = salaryService.getBySalaryId(UUID.fromString(salaryId));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("salaryUpdateForm", salary);
            return "salaryForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報 > 給与修正（post）
    @PostMapping("/salaryForm")
    String salaryFormPost(HttpServletRequest request, @ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            salaryService.update(form);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 講師管理 > 基本情報 > 給与情報削除
    @PostMapping("/deleteSalary")
    String deleteSalary(HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId, @RequestParam("managerId") String managerId) {
        try {
            salaryService.delete(salaryService.getBySalaryId(UUID.fromString(deleteId)));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("manager", managerId);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 給与管理
    @GetMapping("/detail")
    String detail(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            Date dateFrom = DateSet.getDatePeriod(yearNow, monthNow)[0];
            Date dateTo = DateSet.getDatePeriod(yearNow, monthNow)[1];
            String yearBefore = DateSet.getDateBefore(yearNow, monthNow)[0];
            String monthBefore = DateSet.getDateBefore(yearNow, monthNow)[1];
            String yearNext = DateSet.getDateNext(yearNow, monthNow)[0];
            String monthNext = DateSet.getDateNext(yearNow, monthNow)[1];
            User user = userService.getByUserId(UUID.fromString(userId));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Map<UUID, String> supportSalaryMap = new HashMap<>();
            Map<UUID, String> carfareMap = new HashMap<>();
            int sumSalaryPre[] = new int[17];
            int sumSalary[] = new int[17];
            String sumSalaryFormatted[] = new String[20];
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            double setDouble[] = new double[16];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            int incomeTaxValue = 0;
            String incomeTaxFormatted;
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            Lock lock = lockService.getByTarget(manager.getId(), user.getId(), Integer.parseInt(yearNow), Integer.parseInt(monthNow));
            String lockStatus;
            if (lock == null || lock.getStatus() == false) {
                lockStatus = "false";
            } else {
                lockStatus = "true";
            }
            try {
                sumSalary = workService.calcSumSalary(UUID.fromString(userId), dateFrom, dateTo, salary.getClassSalary(), salary.getOfficeSalary(), salary.getSupportSalary());
                for (Work work : workList) {
                    setDouble = workService.calcSumSalary(work, salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                    supportSalaryMap.put(work.getId(), formatter.format(salaryService.getByDate(UUID.fromString(userId), work.getDate()).getSupportSalary()));
                    carfareMap.put(work.getId(), formatter.format(work.getCarfare()));
                    for (int i = 0; i < 16; i++) {
                        resultDouble[i]+= setDouble[i]; 
                    }
                }
                for (int i = 0; i < 16; i++) {
                    sumSalaryPre[i] = (int)Math.ceil(resultDouble[i]);
                    if (i != 0 && i != 2 && i != 3 && i != 5 && i != 7 && i != 10 && i != 12 && i != 14) {
                        sumSalaryPre[16] += sumSalaryPre[i];
                    }
                }
                sumSalary[16] += sumSalaryPre[15] - sumSalary[15] + sumSalaryPre[13] - sumSalary[13] + sumSalaryPre[9] - sumSalary[9] + sumSalaryPre[8] - sumSalary[8] + sumSalaryPre[6] - sumSalary[6] + sumSalaryPre[4] - sumSalary[4];
                sumSalary[15] = sumSalaryPre[15];
                sumSalary[13] = sumSalaryPre[13];
                sumSalary[9] = sumSalaryPre[9];
                sumSalary[8] = sumSalaryPre[8];
                sumSalary[6] = sumSalaryPre[6];
                sumSalary[4] = sumSalaryPre[4];
                sumSalary[10] = sumSalaryPre[16] - sumSalary[16];
                sumSalary[16] += sumSalary[10];
            } catch (Exception e) {
                for (int i = 0; i < 17; i++) {
                    sumSalary[i] = 0;
                    sumSalaryPre[i]= 0; 
                }
            }
            for (Work work : workList) {
                if (!work.getTimeStart().equals("     ") && Integer.valueOf(work.getTimeStart().split(":")[0]) < 10) {
                    work.setTimeStart(Integer.toString(Integer.valueOf(work.getTimeStart().split(":")[0])) + ":" + work.getTimeStart().split(":")[1]);
                }
                if (!work.getTimeEnd().equals("     ") && Integer.valueOf(work.getTimeEnd().split(":")[0]) < 10) {
                    work.setTimeEnd(Integer.toString(Integer.valueOf(work.getTimeEnd().split(":")[0])) + ":" + work.getTimeEnd().split(":")[1]);
                }
                if (!work.getOfficeTimeStart().equals("     ") && Integer.valueOf(work.getOfficeTimeStart().split(":")[0]) < 10) {
                    work.setOfficeTimeStart(Integer.toString(Integer.valueOf(work.getOfficeTimeStart().split(":")[0])) + ":" + work.getOfficeTimeStart().split(":")[1]);
                }
                if (!work.getOfficeTimeEnd().equals("     ") && Integer.valueOf(work.getOfficeTimeEnd().split(":")[0]) < 10) {
                    work.setOfficeTimeEnd(Integer.toString(Integer.valueOf(work.getOfficeTimeEnd().split(":")[0])) + ":" + work.getOfficeTimeEnd().split(":")[1]);
                }
                if (!work.getOtherTimeStart().equals("     ") && Integer.valueOf(work.getOtherTimeStart().split(":")[0]) < 10) {
                    work.setOtherTimeStart(Integer.toString(Integer.valueOf(work.getOtherTimeStart().split(":")[0])) + ":" + work.getOtherTimeStart().split(":")[1]);
                }
                if (!work.getOtherTimeEnd().equals("     ") && Integer.valueOf(work.getOtherTimeEnd().split(":")[0]) < 10) {
                    work.setOtherTimeEnd(Integer.toString(Integer.valueOf(work.getOtherTimeEnd().split(":")[0])) + ":" + work.getOtherTimeEnd().split(":")[1]);
                }
            }
            incomeTaxFormatted = formatter.format(incomeTaxValue);
            for (int i = 0; i < sumSalaryFormatted.length; i++) {
                if (i == 1 || i == 4 || i == 6 || i == 8 || i == 9 || i == 10 || i == 11 || i == 13 || i == 15 || i == 16) {
                    sumSalaryFormatted[i] = formatter.format(sumSalary[i]);
                } else if (i == 17) {
                    sumSalaryFormatted[i] = formatter.format(salary.getCarfare());
                } else if (i == 18) {
                    sumSalaryFormatted[i] = formatter.format(salary.getClassSalary());
                } else if (i == 19) {
                    sumSalaryFormatted[i] = formatter.format(sumSalary[4] + sumSalary[8] + sumSalary[13] + sumSalary[15]);
                } else {
                    sumSalaryFormatted[i] = Integer.toString(sumSalary[i]);
                }
            }
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("salary", salary);
            model.addAttribute("workList", workList);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("sumSalaryFormatted", sumSalaryFormatted);
            model.addAttribute("supportSalaryMap", supportSalaryMap);
            model.addAttribute("carfareMap", carfareMap);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            model.addAttribute("incomeTaxFormatted", incomeTaxFormatted);
            model.addAttribute("lockStatus", lockStatus);
            return "detail";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 給与管理 > シフト登録（get）
    @GetMapping("/createForm")
    String createFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String yearBefore = DateSet.getDateBefore(yearNow, monthNow)[0];
            String monthBefore = DateSet.getDateBefore(yearNow, monthNow)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Work work = new Work();
            work.setUserId(user.getId());
            work.setCarfare(salary.getCarfare());
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("workCreateForm", work);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            return "createForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 給与管理 > シフト登録（post）
    @PostMapping("/createForm")
    String createFormPost(HttpServletRequest request, @ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.add(form);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:detail";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/detail", domainAWS);
                    return redirectUrl;
                }
            } catch (Exception e) {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:createForm";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/createForm", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 給与管理 > シフト修正（get）
    @GetMapping("/setForm")
    String setFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String yearNow, @RequestParam("month") String monthNow) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            Work work = workService.findWorkById(UUID.fromString(editId));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("user", user);
            model.addAttribute("workUpdateForm", work);
            model.addAttribute("yearNow", yearNow);
            model.addAttribute("monthNow", monthNow);
            return "setForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 給与管理 > シフト修正（post）
    @PostMapping("/setForm")
    String setFormPost(HttpServletRequest request, @ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("manager", manager);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.update(form);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:detail";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/detail", domainAWS);
                    return redirectUrl;
                }
            } catch (Exception e) {
                redirectAttributes.addAttribute("edit", form.getId());
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:setForm";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/setForm", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // 給与管理 > シフト削除
    @PostMapping("/clearWork")
    String clearWork(HttpServletRequest request, @Param("managerId") String managerId, @Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        try {
            Work work = workService.findWorkById(UUID.fromString(deleteId));
            String year = DateSet.getYear(work.getDate());
            String month = DateSet.getMonth(work.getDate());
            workService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("manager", managerId);
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:detail";
            } else {
                String redirectUrl = String.format("redirect:https://%s/detail", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // 給与管理 > ロック状態変更
    @GetMapping("/changeStatus")
    String changeStatus(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            // ロック状態を変更・削除する
            Lock lock = lockService.getByTarget(UUID.fromString(managerId), UUID.fromString(userId), Integer.parseInt(year), Integer.parseInt(month));
            if (lock == null) {
                // ロックする（add）
                lock = new Lock();
                lock.setClassAreaId(UUID.fromString(managerId));
                lock.setUserId(UUID.fromString(userId));
                lock.setYear(Integer.parseInt(year));
                lock.setMonth(Integer.parseInt(month));
                lock.setStatus(true);
                lockService.add(lock);
            } else if (lock.getStatus() == false) {
                // ロックする（update）
                lock.setStatus(true);
                lockService.update(lock);
            } else {
                // ロックを解除する（delete）
                lockService.deleteById(lock);
            }

            // リダイレクト時のパラメータを設定
            redirectAttributes.addAttribute("manager", managerId);
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);

            // リダイレクト
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:detail";
            } else {
                String redirectUrl = String.format("redirect:https://%s/detail", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }

    }
    
    // 給与管理 > Excelファイルのエクスポート
    @GetMapping("/downloadExcel")
    void downloadExcel(HttpServletRequest request, HttpServletResponse response, @RequestParam("manager") String managerId, @RequestParam("year") String year, @RequestParam("month") String month, @RequestParam("lock") String lock) {
        try {
            // 情報の取得
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            List<User> userList = userService.findByClassAreaId(manager.getId());

            // シート名データ
            String numberList[] = {"①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩", "⑪", "⑫", "⑬", "⑭", "⑮", "⑯", "⑰", "⑱"};

            // レスポンスヘッダーの設定
            String fileName = URLEncoder.encode("講師給フォーム("+year.substring(2, 4)+manager.getClassArea()+")"+year+"."+String.valueOf(Integer.parseInt(month)), StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
            
            // テンプレートファイルを読み込み
            ClassPathResource templateResource = new ClassPathResource("static/excel/excelTemplate.xlsx");
            InputStream templateInputStream = templateResource.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(templateInputStream);

            // トップシートの取得・初期設定
            Sheet sheetTop = workbook.getSheetAt(0);
            Cell topYear = sheetTop.getRow(0).getCell(6);
            Cell topMonth = sheetTop.getRow(0).getCell(8);
            Cell topClass = sheetTop.getRow(0).getCell(10);
            topYear.setCellValue(Integer.parseInt(year));
            topMonth.setCellValue(Integer.parseInt(month));
            topClass.setCellValue(manager.getClassArea());

            // シートの入力（全講師）
            for (int i = 0; i < userList.size(); i++) {
                // 講師情報の取得
                User u = userList.get(i);

                // 勤務情報の取得
                List<Work> workList = workService.findByUserId(u.getId(), dateFrom, dateTo);

                // 講師級シートの取得
                XSSFSheet sheet = workbook.getSheetAt(i + 1);
                String sheetName = numberList[i]+u.getUserName().split(" ")[0];
                workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);

                // cellへデータ入力
                String headers[] = new String[8];
                int cellPointsInHeader[] = {4, 16, 25, 29, 33, 35, 40, 42};
                for (int j = 0; j < headers.length; j++) {
                    headers[j] = "";
                }
                headers[0] = manager.getClassArea();
                headers[1] = u.getUserName();
                headers[2] = year;
                headers[3] = Integer.toString(Integer.valueOf(month));
                headers[4] = Integer.toString(Integer.valueOf(DateSet.getDateBefore(year, month)[1]));
                headers[5] = "月26日";
                headers[6] = Integer.toString(Integer.valueOf(month));
                headers[7] = "月25日";
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    headerRow = sheet.createRow(0);
                }
                for (int j = 0; j < headers.length; j++) {
                    Cell cell = headerRow.getCell(cellPointsInHeader[j]);
                    if (cell == null) {
                        cell = headerRow.createCell(cellPointsInHeader[j]);
                    }
                    if (j == 2 || j == 3 || j == 4 || j == 6) {
                        cell.setCellValue(Integer.valueOf(headers[j]));
                    } else if (j == 0 || j == 1) {
                        cell.setCellValue(headers[j]);
                    }
                }
                Cell carfareCell = sheet.getRow(3).getCell(39);
                int carfareTarget = salaryService.getByDate(u.getId(), year+"-"+month+"-25").getCarfare();
                if (carfareTarget != 0) {
                    carfareCell.setCellValue(carfareTarget);
                }
                int sumInt[] = new int[11];
                for (int j = 0; j < sumInt.length; j++) {
                    sumInt[j] = 0;
                }
                int rowIndex = 7;
                int cellPointsInWork[] = {1, 4, 5, 8, 10, 13, 15, 17, 19, 21, 23, 25, 27, 29, 33, 35, 37, 39, 41, 43, 45};
                for (Work work : workList) {
                    String values[] = new String[21];
                    for (int j = 0; j < values.length; j++) {
                        values[j] = "";
                    }
                    values[0] = String.valueOf(Integer.parseInt(work.getDate().split("-")[0]))+"/"+String.valueOf(Integer.parseInt(work.getDate().split("-")[1]))+"/"+String.valueOf(Integer.parseInt(work.getDate().split("-")[2]));
                    values[1] = work.getDayOfWeek();
                    if (work.getClassM() == true) {
                        values[2] += "M";
                    }
                    if (work.getClassK() == true) {
                        values[2] += "K";
                    }
                    if (work.getClassS() == true) {
                        values[2] += "S";
                    }
                    if (work.getClassA() == true) {
                        values[2] += "A";
                    }
                    if (work.getClassB() == true) {
                        values[2] += "B";
                    }
                    if (work.getClassC() == true) {
                        values[2] += "C";
                    }
                    if (work.getClassD() == true) {
                        values[2] += "D";
                    }
                    if (work.getClassCount() != 0) {
                        values[3] = Integer.toString(work.getClassCount());
                    }
                    if (!work.getHelpArea().equals("")) {
                        values[4] = work.getHelpArea();
                    }
                    if (!work.getTimeStart().equals("     ")) {
                        if (Integer.valueOf(work.getTimeStart().split(":")[0]) < 10) {
                            values[5] = Integer.toString(Integer.valueOf(work.getTimeStart().split(":")[0])) + ":" + work.getTimeStart().split(":")[1];
                        } else {
                            values[5] = work.getTimeStart();
                        }
                    }
                    if (!work.getTimeEnd().equals("     ")) {
                        if (Integer.valueOf(work.getTimeEnd().split(":")[0]) < 10) {
                            values[6] = Integer.toString(Integer.valueOf(work.getTimeEnd().split(":")[0])) + ":" + work.getTimeEnd().split(":")[1];
                        } else {
                            values[6] = work.getTimeEnd();
                        }
                    }
                    if (work.getBreakTime() != 0) {
                        values[7] = Integer.toString(work.getBreakTime()/60)+":"+String.format("%02d", work.getBreakTime()%60);
                    }
                    if (!work.getOfficeTimeStart().equals("     ")) {
                        if (Integer.valueOf(work.getOfficeTimeStart().split(":")[0]) < 10) {
                            values[8] = Integer.toString(Integer.valueOf(work.getOfficeTimeStart().split(":")[0])) + ":" + work.getOfficeTimeStart().split(":")[1];
                        } else {
                            values[8] = work.getOfficeTimeStart();
                        }
                    }
                    if (!work.getOfficeTimeEnd().equals("     ")) {
                        if (Integer.valueOf(work.getOfficeTimeEnd().split(":")[0]) < 10) {
                            values[9] = Integer.toString(Integer.valueOf(work.getOfficeTimeEnd().split(":")[0])) + ":" + work.getOfficeTimeEnd().split(":")[1];
                        } else {
                            values[9] = work.getOfficeTimeEnd();
                        }
                    }
                    if (work.getOfficeTime() != 0) {
                        values[10] = Integer.toString(work.getOfficeTime()/60)+":"+String.format("%02d", work.getOfficeTime()%60);
                    }
                    if (work.getSupportSalary().equals("true")) {
                        values[11] = Integer.toString(salaryService.getByDate(u.getId(), work.getDate()).getSupportSalary());
                    }
                    if (work.getCarfare() != 0) {
                        values[12] = Integer.toString(work.getCarfare());
                    } else {
                        values[12] = "0";
                    }
                    if (!work.getOtherWork().equals("")) {
                        values[13] = work.getOtherWork();
                    }
                    if (!work.getOtherTimeStart().equals("     ")) {
                        if (Integer.valueOf(work.getOtherTimeStart().split(":")[0]) < 10) {
                            values[14] = Integer.toString(Integer.valueOf(work.getOtherTimeStart().split(":")[0])) + ":" + work.getOtherTimeStart().split(":")[1];
                        } else {
                            values[14] = work.getOtherTimeStart();
                        }
                    }
                    if (!work.getOtherTimeEnd().equals("     ")) {
                        if (Integer.valueOf(work.getOtherTimeEnd().split(":")[0]) < 10) {
                            values[15] = Integer.toString(Integer.valueOf(work.getOtherTimeEnd().split(":")[0])) + ":" + work.getOtherTimeEnd().split(":")[1];
                        } else {
                            values[15] = work.getOtherTimeEnd();
                        }
                    }
                    if (work.getOtherBreakTime() != 0) {
                        values[16] = Integer.toString(work.getOtherBreakTime()/60)+":"+String.format("%02d", work.getOtherBreakTime()%60);
                    }
                    if (work.getOtherTime() != 0) {
                        values[17] = Integer.toString(work.getOtherTime()/60)+":"+String.format("%02d", work.getOtherTime()%60);
                    }
                    if (work.getOutOfTime() != 0) {
                        values[18] = Integer.toString(work.getOutOfTime()/60)+":"+String.format("%02d", work.getOutOfTime()%60);
                    }
                    if (work.getOverTime() != 0) {
                        values[19] = Integer.toString(work.getOverTime()/60)+":"+String.format("%02d", work.getOverTime()%60);
                    }
                    if (work.getNightTime() != 0) {
                        values[20] = Integer.toString(work.getNightTime()/60)+":"+String.format("%02d", work.getNightTime()%60);
                    }
                    sumInt[0] += work.getClassCount();
                    sumInt[1] += 1;
                    sumInt[2] += work.getOfficeTime();
                    if (work.getSupportSalary().equals("true")) {
                        sumInt[3] += salaryService.getByDate(u.getId(), work.getDate()).getSupportSalary();
                    }
                    sumInt[4] += work.getCarfare();
                    sumInt[5] += work.getOtherTime();
                    sumInt[6] += work.getOutOfTime();
                    sumInt[7] += work.getOverTime();
                    sumInt[8] += work.getNightTime();
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) {
                        row = sheet.createRow(rowIndex);
                    }
                    Cell cells[] = new Cell[values.length];
                    for (int j = 0; j < cells.length; j++) {
                        cells[j] = row.getCell(cellPointsInWork[j]);
                        if (cells[j] == null) {
                            cells[j] = row.createCell(cellPointsInWork[j]);
                        }
                        if (j == 0) {
                            String infoDate[] = values[j].split("/");
                            LocalDate localDate = LocalDate.of(Integer.parseInt(infoDate[0]), Integer.parseInt(infoDate[1]), Integer.parseInt(infoDate[2]));
                            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());;
                            cells[j].setCellValue(date);
                        }
                        if (j == 2 || j == 4 || j == 5 || j == 6 || j == 7 || j == 8 || j == 9 || j == 13 || j == 14 || j == 15 || j == 16) {
                            if (!values[j].equals("")) {
                                cells[j].setCellValue(values[j]);
                            }
                        } else if (j == 12) {
                            if (Integer.valueOf(values[j]) != carfareTarget) {
                                if (cells[j].getCellType() == CellType.FORMULA) {
                                    cells[j].setBlank();
                                    cells[j].setCellValue(Integer.valueOf(values[j]));
                                }
                            }
                        }
                    }
                    rowIndex += 1;
                }

                // トップシートの入力
                Row rowTop = sheetTop.getRow(i + 33);
                if (rowTop == null) {
                    rowTop = sheetTop.createRow(i + 33);
                }
                for (int j = 0; j < 13; j++) {
                    Cell cellTop = rowTop.getCell(j);
                    if (j == 1) {
                        // 講師No.
                        cellTop.setCellValue(u.getTeacherNo());
                    } else if (j == 2) {
                        // 講師氏名
                        cellTop.setCellValue(u.getUserName());
                    } else if (j == 3) {
                        // 退職日
                        if (!u.getState()) {
                            String retireYear = u.getRetireDate().split("-")[0];
                            String retireMonth = String.valueOf(Integer.parseInt(u.getRetireDate().split("-")[1]));
                            String retireValue = retireYear + "年" + retireMonth + "月";
                            cellTop.setCellValue(retireValue);
                        }
                    }
                }
                if (!u.getState()) {
                    for (int j = 1; j < 4; j++) {
                        Cell cellTop = rowTop.getCell(j);
                        CellStyle originalStyle = cellTop.getCellStyle();
                        CellStyle newStyle = workbook.createCellStyle();
                        newStyle.cloneStyleFrom(originalStyle);
                        newStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                        newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cellTop.setCellStyle(newStyle);
                    }
                    byte[] rgbColor = new byte[] {
                        (byte) 204,
                        (byte) 204,
                        (byte) 204
                    };
                    XSSFColor customColor = new XSSFColor(rgbColor, workbook.getStylesSource().getIndexedColors());
                    sheet.setTabColor(customColor);
                }
            }

            // 新採用講師シート
            for (int i = userList.size(); i < 18; i++) {
                Sheet sheet = workbook.getSheetAt(i + 1);
                String sheetName = numberList[i]+"新採用"+Integer.toString(i - userList.size() + 1);
                workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);
                Row row = sheet.getRow(0);
                row.getCell(4).setCellValue(manager.getClassArea());
                row.getCell(25).setCellValue(Integer.parseInt(year));
                row.getCell(29).setCellValue(Integer.parseInt(month));
                row.getCell(33).setCellValue(Integer.parseInt(DateSet.getDateBefore(year, month)[1]));
                row.getCell(40).setCellValue(Integer.parseInt(month));
            }

            // ファイルオープン時に関数を再計算させる
            workbook.setForceFormulaRecalculation(true);

            // Excelデータをレスポンスに書き込む
            workbook.write(response.getOutputStream());
            workbook.close();
            response.getOutputStream().flush();

            // ロックステータスの変更
            if (lock.equals("true")) {
                for (User u : userList) {
                    Lock lockStatus = lockService.getByTarget(manager.getId(), u.getId(), Integer.parseInt(year), Integer.parseInt(month));
                    if (lockStatus == null) {
                        lockStatus = new Lock();
                        lockStatus.setClassAreaId(manager.getId());
                        lockStatus.setUserId(u.getId());
                        lockStatus.setYear(Integer.parseInt(year));
                        lockStatus.setMonth(Integer.parseInt(month));
                        lockStatus.setStatus(true);
                        lockService.add(lockStatus);
                    } else if (lockStatus.getStatus() == false) {
                        lockStatus.setStatus(true);
                        lockService.update(lockStatus);
                    }
                }
            }

        } catch (Exception e) {
            // エラー処理
            e.printStackTrace();
        }
    }

    // アカウント
    @GetMapping("/infoManager")
    String infoManager(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "infoManager";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }
    
    // アカウント > アカウント情報修正（get）
    @GetMapping("/editManagerForm")
    String editManagerFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("edit") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("managerUpdateForm", manager);
            return "editManagerForm";
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // アカウント > アカウント情報修正（post）
    @PostMapping("/editManagerForm")
    String editManagerFormPost(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, @ModelAttribute("managerUpdateForm") Manager manager) {
        try {
            managerService.update(manager);
            Cookie cookieLoginId = new Cookie("managerLoginId", manager.getLoginId());
            cookieLoginId.setMaxAge(30 * 24 * 60 * 60);
            cookieLoginId.setHttpOnly(true);
            cookieLoginId.setPath("/");
            response.addCookie(cookieLoginId);
            Cookie cookiePassword = new Cookie("managerPassword", manager.getPassword());
            cookiePassword.setMaxAge(30 * 24 * 60 * 60);
            cookiePassword.setHttpOnly(true);
            cookiePassword.setPath("/");
            response.addCookie(cookiePassword);
            redirectAttributes.addAttribute("manager", manager.getId());
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoManager", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

    // アカウント > アカウント削除
    @GetMapping("/deleteManager")
    String deleteManager(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, @RequestParam("delete") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            for (User user : userList) {
                for (Work work : workService.findAllByUserId(user.getId())) {
                    workService.deleteById(work.getId());
                }
                for (Salary salary : salaryService.findByUserId(user.getId())) {
                    salaryService.delete(salary);
                }
                for (WorkTemplate template : workTemplateService.findByUserId(user.getId())) {
                    workTemplateService.deleteById(template.getId());
                }
                for (Lock lock : lockService.findByUserId(user.getId())) {
                    lockService.deleteById(lock);
                }
                userService.deleteById(user.getId());
            }
            Cookie cookieLoginId = new Cookie("managerLoginId", null);
            cookieLoginId.setMaxAge(0);
            cookieLoginId.setPath("/");
            response.addCookie(cookieLoginId);
            Cookie cookiePassword = new Cookie("managerPassword", null);
            cookiePassword.setMaxAge(0);
            cookiePassword.setPath("/");
            response.addCookie(cookiePassword);
            managerService.deleteById(manager);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in deleteManager(get)");
            e.printStackTrace();
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:loginManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/loginManager", domainAWS);
                return redirectUrl;
            }
        }
    }

}
