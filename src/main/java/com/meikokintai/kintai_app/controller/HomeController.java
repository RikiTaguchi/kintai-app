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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.net.URLEncoder;
import java.text.NumberFormat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.meikokintai.kintai_app.model.Manager;
import com.meikokintai.kintai_app.model.Salary;
import com.meikokintai.kintai_app.model.User;
import com.meikokintai.kintai_app.model.Work;
import com.meikokintai.kintai_app.model.WorkTemplate;
import com.meikokintai.kintai_app.service.IncomeTaxService;
import com.meikokintai.kintai_app.service.ManagerService;
import com.meikokintai.kintai_app.service.SalaryService;
import com.meikokintai.kintai_app.service.UserService;
import com.meikokintai.kintai_app.service.WorkService;
import com.meikokintai.kintai_app.service.WorkTemplateService;
import com.meikokintai.kintai_app.util.DateSet;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
    
    private final WorkService workService;
    private final UserService userService;
    private final ManagerService managerService;
    private final SalaryService salaryService;
    private final WorkTemplateService workTemplateService;
    private final IncomeTaxService incomeTaxService;

    // ドメイン名(ローカル用)
    private final String domainLocal = "localhost:8080";

    // ドメイン名(AWS本番環境用)
    private final String domainAWS = "meikokintai.com";
    
    public HomeController(WorkService workService, UserService userService, ManagerService managerService, SalaryService salaryService, WorkTemplateService workTemplateService, IncomeTaxService incomeTaxService) {
        this.workService = workService;
        this.userService = userService;
        this.managerService = managerService;
        this.salaryService = salaryService;
        this.workTemplateService = workTemplateService;
        this.incomeTaxService = incomeTaxService;
    }
    
    // 講師ホーム画面
    @GetMapping("/index")
    String index(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            String yearNext = DateSet.getDateNext(year, month)[0];
            String monthNext = DateSet.getDateNext(year, month)[1];
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            int sumSalary[] = new int[16];
            double setDouble[] = new double[15];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
            for (Work work : workList) {
                setDouble = workService.calcSumSalary(work, salaryService.getByDate(UUID.fromString(userId), work.getDate()));
                for (int i = 0; i < 15; i++) {
                    resultDouble[i]+= setDouble[i]; 
                }
            }
            for (int i = 0; i < 15; i++) {
                sumSalary[i] = (int)Math.ceil(resultDouble[i]);
                if (i != 0 && i != 2 && i != 3 && i != 5 && i != 7 && i != 11 && i != 13) {
                    sumSalary[15] += sumSalary[i];
                }
            }
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workList", workList);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
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
    
    // 社員ホーム画面
    @GetMapping("/indexManager")
    String indexManager(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(UUID.fromString(managerId));
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("manager", managerId);
            return "indexManager";
        } catch (Exception e) {
            System.out.println("Error happened in indexManager.html");
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
    
    // 給与詳細（講師用）
    @GetMapping("/detail")
    String detail(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month, @RequestParam("tax") String tax) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            String yearNext = DateSet.getDateNext(year, month)[0];
            String monthNext = DateSet.getDateNext(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Map<UUID, Salary> salaryMap = new HashMap<>();
            int sumSalaryPre[] = new int[17];
            int sumSalary[] = new int[17];
            String sumSalaryFormatted[] = new String[19];
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            double setDouble[] = new double[16];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            int incomeTax;
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
                        incomeTax = incomeTaxService.getByTotalIncome(totalIncome).getTax();
                        sumSalary[16] -= incomeTax;
                    } else {
                        incomeTax = 0;
                    }
                } else {
                    incomeTax = 0;
                }
            } else {
                incomeTax = 0;
            }
            incomeTaxFormatted = formatter.format(incomeTax);
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
            model.addAttribute("salary", salary);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("sumSalaryFormatted", sumSalaryFormatted);
            model.addAttribute("salaryMap", salaryMap);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            model.addAttribute("tax", tax);
            model.addAttribute("incomeTaxFormatted", incomeTaxFormatted);
            redirectAttributes.addAttribute("user", userId);
            return "detail";
        } catch (Exception e) {
            System.out.println("Error happened in detail.html");
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
    
    // 勤務詳細（講師用）
    @GetMapping("/detailWork")
    String detailWork(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("detail") String detailId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Work work = workService.findWorkById(UUID.fromString(detailId));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), work.getDate());
            String supportSalaryFormatted = String.format("%,d", salary.getSupportSalary());
            String carfareFormatted = String.format("%,d", work.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("supportSalaryFormatted", supportSalaryFormatted);
            model.addAttribute("carfareFormatted", carfareFormatted);
            model.addAttribute("work", work);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "detailWork";
        } catch (Exception e) {
            System.out.println("Error happened in detailWork.html");
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
    
    // 給与詳細（社員用）
    @GetMapping("/detailUser")
    String detailUser(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @Param("year") String year, @Param("month") String month, @Param("tax") String tax) {
        try {
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            String yearNext = DateSet.getDateNext(year, month)[0];
            String monthNext = DateSet.getDateNext(year, month)[1];
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Map<UUID, String> supportSalaryMap = new HashMap<>();
            Map<UUID, String> carfareMap = new HashMap<>();
            int sumSalaryPre[] = new int[17];
            int sumSalary[] = new int[17];
            String sumSalaryFormatted[] = new String[20];
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
            double setDouble[] = new double[16];
            double resultDouble[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            int incomeTax;
            String incomeTaxFormatted;
            List<Work> workList = workService.findByUserId(UUID.fromString(userId), dateFrom, dateTo);
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
            if (tax.equals("on")) {
                int totalIncome = sumSalary[1] + sumSalary[4] + sumSalary[8] + sumSalary[6] + sumSalary[9] + sumSalary[10] + sumSalary[13]  + sumSalary[15];
                if (totalIncome >= 88000) {
                    if (incomeTaxService.getByTotalIncome(totalIncome) != null) {
                        incomeTax = incomeTaxService.getByTotalIncome(totalIncome).getTax();
                        sumSalary[16] -= incomeTax;
                    } else {
                        incomeTax = 0;
                    }
                } else {
                    incomeTax = 0;
                }
            } else {
                incomeTax = 0;
            }
            incomeTaxFormatted = formatter.format(incomeTax);
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
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("workList", workList);
            model.addAttribute("sumSalary", sumSalary);
            model.addAttribute("sumSalaryFormatted", sumSalaryFormatted);
            model.addAttribute("supportSalaryMap", supportSalaryMap);
            model.addAttribute("carfareMap", carfareMap);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("yearBefore", yearBefore);
            model.addAttribute("monthBefore", monthBefore);
            model.addAttribute("yearNext", yearNext);
            model.addAttribute("monthNext", monthNext);
            model.addAttribute("tax", tax);
            model.addAttribute("incomeTaxFormatted", incomeTaxFormatted);
            redirectAttributes.addAttribute("manager", managerId);
            return "detailUser";
        } catch (Exception e) {
            System.out.println("Error happened in detailUser.html");
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
    
    // 給与推移（講師用）
    @GetMapping("/detailSalary")
    String detailSalary(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            Map<UUID, String[]> salaryMapFormatted = new HashMap<>();
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
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("salaryMapFormatted", salaryMapFormatted);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "detailSalary";
        } catch (Exception e) {
            System.out.println("Error happened in detailSalary.html");
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
    
    // テンプレート詳細（講師用）
    @GetMapping("/detailTemplate")
    String detailTemplate(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("template") String templateId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            WorkTemplate template = workTemplateService.findTemplateById(UUID.fromString(templateId));
            String carfareFormatted = String.format("%,d", template.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("template", template);
            model.addAttribute("carfareFormatted", carfareFormatted);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "detailTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in detailTemplate.html");
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
    
    // 給与推移情報（社員用）
    @GetMapping("/infoSalary")
    String infoSalary(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @Param("year") String year, @Param("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            Map<UUID, String[]> salaryMapFormatted = new HashMap<>();
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
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("salaryMapFormatted", salaryMapFormatted);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "infoSalary";
        } catch (Exception e) {
            System.out.println("Error happened in infoSalary.html");
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
    
    // 講師基本情報（社員用）
    @GetMapping("/infoUser")
    String infoUser(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            Calendar calendar = Calendar.getInstance();
            String year = String.format("%04d", calendar.get(Calendar.YEAR));
            String month = String.format("%02d", calendar.get(Calendar.MONTH)+1);
            String day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), year+"-"+month+"-"+day);
            String salaryFormatted[] = new String[4];
            salaryFormatted[0] = String.format("%,d", salary.getClassSalary());
            salaryFormatted[1] = String.format("%,d", salary.getOfficeSalary());
            salaryFormatted[2] = String.format("%,d", salary.getSupportSalary());
            salaryFormatted[3] = String.format("%,d", salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("salaryFormatted", salaryFormatted);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            return "infoUser";
        } catch (Exception e) {
            System.out.println("Error happened in infoUser.html");
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
    
    // テンプレート一覧（講師用）
    @GetMapping("/infoTemplate")
    String infoTemplate(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("templateList", templateList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "infoTemplate";
        } catch (Exception e) {
            System.out.println("Error happened in infoTemplate.html");
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

    // アカウント情報（社員用）
    @GetMapping("/infoManager")
    String infoManager(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            List<User> userList = userService.findByClassAreaId(manager.getId());
            model.addAttribute("manager", manager);
            model.addAttribute("userList", userList);
            return "infoManager";
        } catch (Exception e) {
            System.out.println("Error happened in infoManager.html");
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
    
    // シフト登録（講師用）
    @GetMapping("/addForm")
    String addFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Work work = new Work();
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            work.setUserId(user.getId());
            work.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workCreateForm", work);
            model.addAttribute("templateList", templateList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "addForm";
        } catch (Exception e) {
            System.out.println("Error happened in addForm.html");
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
    
    // テンプレート登録（講師用）
    @GetMapping("/templateForm")
    String templateFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            WorkTemplate template = new WorkTemplate();
            template.setUserId(user.getId());
            template.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("templateCreateForm", template);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "templateForm";
        } catch (Exception e) {
            System.out.println("Error happened in templateForm.html");
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
    
    // シフト登録（社員用）
    @GetMapping("/createForm")
    String createFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month, @RequestParam("tax") String tax) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            Work work = new Work();
            work.setUserId(user.getId());
            work.setCarfare(salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workCreateForm", work);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("tax", tax);
            redirectAttributes.addAttribute("user", userId);
            return "createForm";
        } catch (Exception e) {
            System.out.println("Error happened in createForm.html");
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
    
    // シフト修正（講師用）
    @GetMapping("/editForm")
    String editFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Work work = workService.findWorkById(UUID.fromString(editId));
            List<WorkTemplate> templateList = workTemplateService.findByUserId(UUID.fromString(userId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workUpdateForm", work);
            model.addAttribute("templateList", templateList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "editForm";
        } catch (Exception e) {
            System.out.println("Error happened in editForm.html");
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
    
    // テンプレート修正（講師用）
    @GetMapping("/editTemplateForm")
    String editTemplateFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            WorkTemplate template = workTemplateService.findTemplateById(UUID.fromString(editId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("templateUpdateForm", template);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "editTemplateForm";
        } catch (Exception e) {
            System.out.println("Error happened in editTemplateForm.html");
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
    
    // 新規給与情報登録（社員用）
    @GetMapping("/updateForm")
    String updateFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salaryUpdateForm", salary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "updateForm";
        } catch (Exception e) {
            System.out.println("Error happened in updateForm.html");
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
    
    // シフト修正（社員用）
    @GetMapping("/setForm")
    String setFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("edit") String editId, @RequestParam("year") String year, @RequestParam("month") String month, @RequestParam("tax") String tax) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Work work = workService.findWorkById(UUID.fromString(editId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("workUpdateForm", work);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("tax", tax);
            redirectAttributes.addAttribute("user", userId);
            return "setForm";
        } catch (Exception e) {
            System.out.println("Error happened in setForm.html");
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
    
    // 講師基本情報（講師用）
    @GetMapping("/user")
    String user(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Calendar calendar = Calendar.getInstance();
            String yearNow = String.format("%04d", calendar.get(Calendar.YEAR));
            String monthNow = String.format("%02d", calendar.get(Calendar.MONTH)+1);
            String dayNow = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH));
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearNow+"-"+monthNow+"-"+dayNow);
            List<Salary> salaryList = salaryService.findByUserId(UUID.fromString(userId));
            String salaryFormatted[] = new String[4];
            salaryFormatted[0] = String.format("%,d", salary.getClassSalary());
            salaryFormatted[1] = String.format("%,d", salary.getOfficeSalary());
            salaryFormatted[2] = String.format("%,d", salary.getSupportSalary());
            salaryFormatted[3] = String.format("%,d", salary.getCarfare());
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("salaryFormatted", salaryFormatted);
            model.addAttribute("salaryList", salaryList);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "user";
        } catch (Exception e) {
            System.out.println("Error happened in user.html");
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
    
    // 講師アカウント情報修正（講師用）
    @GetMapping("/userForm")
    String userFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            String yearBefore = DateSet.getDateBefore(year, month)[0];
            String monthBefore = DateSet.getDateBefore(year, month)[1];
            Salary salary = salaryService.getByDate(UUID.fromString(userId), yearBefore+"-"+monthBefore+"-26");
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salary", salary);
            model.addAttribute("userUpdateForm", user);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "userForm";
        } catch (Exception e) {
            System.out.println("Error happened in userForm.html");
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
    
    // 給与情報修正（社員用）
    @GetMapping("/salaryForm")
    String salaryFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId, @RequestParam("user") String userId, @RequestParam("salary") String salaryId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Salary salary = salaryService.getBySalaryId(UUID.fromString(salaryId));
            model.addAttribute("user", user);
            model.addAttribute("manager", manager);
            model.addAttribute("salaryUpdateForm", salary);
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            redirectAttributes.addAttribute("user", userId);
            return "salaryForm";
        } catch (Exception e) {
            System.out.println("Error happened in salaryForm.html");
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
    
    // シフト登録（講師用）
    @PostMapping("/addForm")
    String addFormPost(HttpServletRequest request, @ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.add(form);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:index";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/index", domainAWS);
                    return redirectUrl;
                }
            } catch (Exception e) {
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:addForm";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/addForm", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            System.out.println("Error happened in addForm(post)");
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
    
    // テンプレート登録（講師用）
    @PostMapping("/templateForm")
    String templateFormPost(HttpServletRequest request, @ModelAttribute("templateCreateForm") WorkTemplate form, RedirectAttributes redirectAttributes, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            workTemplateService.add(form);
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoTemplate";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoTemplate", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in templateForm(post)");
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
    
    // シフト登録（社員用）
    @PostMapping("/createForm")
    String createFormPost(HttpServletRequest request, @ModelAttribute("workCreateForm") Work form, RedirectAttributes redirectAttributes, @RequestParam("tax") String tax) {
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
            redirectAttributes.addAttribute("tax", tax);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.add(form);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:detailUser";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/detailUser", domainAWS);
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
            System.out.println("Error happened in createForm(post)");
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
    
    // シフト修正（講師用）
    @PostMapping("/editForm")
    String editFormPost(HttpServletRequest request, @ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes) {
        try {
            String year = DateSet.getYear(form.getDate());
            String month = DateSet.getMonth(form.getDate());
            String dayOfWeek = DateSet.getDayOfWeek(form.getDate());
            User user = userService.getByUserId(form.getUserId());
            form.setDayOfWeek(dayOfWeek);
            form.setSupportSalary("true");
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("detail", form.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.update(form);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:detailWork";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/detailWork", domainAWS);
                    return redirectUrl;
                }
            } catch (Exception e) {
                redirectAttributes.addAttribute("edit", form.getId());
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:editForm";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/editForm", domainAWS);
                    return redirectUrl;
                }
            }
        } catch (Exception e) {
            System.out.println("Error happened in editForm(post)");
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
    
    // シフト修正（社員用）
    @PostMapping("/setForm")
    String setFormPost(HttpServletRequest request, @ModelAttribute("workUpdateForm") Work form, RedirectAttributes redirectAttributes, @RequestParam("tax") String tax) {
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
            redirectAttributes.addAttribute("tax", tax);
            try {
                form = workService.calcTimeAndSalary(form);
                workService.update(form);
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:detailUser";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/detailUser", domainAWS);
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
            System.out.println("Error happened in setForm(post)");
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
    
    // シフト削除（講師用）
    @PostMapping("/deleteWork")
    String deleteWork(HttpServletRequest request, @Param("userId") String userId, @Param("deleteId") String deleteId, RedirectAttributes redirectAttributes) {
        try {
            Work work = workService.findWorkById(UUID.fromString(deleteId));
            String year = DateSet.getYear(work.getDate());
            String month = DateSet.getMonth(work.getDate());
            workService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:index";
            } else {
                String redirectUrl = String.format("redirect:https://%s/index", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in deleteWork(post)");
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
    
    // シフト削除（社員用）
    @PostMapping("/clearWork")
    String clearWork(HttpServletRequest request, @Param("managerId") String managerId, @Param("userId") String userId, @Param("deleteId") String deleteId, @Param("tax") String tax, RedirectAttributes redirectAttributes) {
        try {
            Work work = workService.findWorkById(UUID.fromString(deleteId));
            String year = DateSet.getYear(work.getDate());
            String month = DateSet.getMonth(work.getDate());
            workService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("manager", managerId);
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            redirectAttributes.addAttribute("tax", tax);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:detailUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/detailUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in clearWork(post)");
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
    
    // 講師アカウント情報修正（講師用）
    @PostMapping("/userForm")
    String userFormPost(HttpServletRequest request, HttpServletResponse response, @ModelAttribute("userUpdateForm") User form, RedirectAttributes redirectAttributes) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
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
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:user";
            } else {
                String redirectUrl = String.format("redirect:https://%s/user", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in userForm(post)");
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
    
    // 新規給与情報登録（社員用）
    @PostMapping("/updateForm")
    String updateFormPost(HttpServletRequest request, @ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            salaryService.add(form);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoUser";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoUser", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in updateForm(post)");
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
    
    // 給与情報修正（社員用）
    @PostMapping("/salaryForm")
    String salaryFormPost(HttpServletRequest request, @ModelAttribute("salaryUpdateForm") Salary form, RedirectAttributes redirectAttributes) {
        try {
            Calendar calendar = Calendar.getInstance();
            String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
            User user = userService.getByUserId(form.getUserId());
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            salaryService.update(form);
            redirectAttributes.addAttribute("user", user.getId());
            redirectAttributes.addAttribute("manager", manager.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoSalary";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoSalary", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in salaryForm(post)");
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
    
    // テンプレート修正（講師用）
    @PostMapping("/editTemplateForm")
    String editTemplateFormPost(HttpServletRequest request, @ModelAttribute("templateUpdateForm") WorkTemplate form, RedirectAttributes redirectAttributes, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            workTemplateService.update(form);
            redirectAttributes.addAttribute("user", form.getUserId());
            redirectAttributes.addAttribute("template", form.getId());
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:detailTemplate";
            } else {
                String redirectUrl = String.format("redirect:https://%s/detailTemplate", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in editTemplateForm(post)");
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
    
    // テンプレート削除（講師用）
    @PostMapping("/deleteTemplate")
    String deleteTemplate(HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            workTemplateService.deleteById(UUID.fromString(deleteId));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoTemplate";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoTemplate", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in deleteTemplate(post)");
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

    // 給与情報削除（社員用）
    @PostMapping("/deleteSalary")
    public String deleteSalary(HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("deleteId") String deleteId, @RequestParam("userId") String userId, @RequestParam("managerId") String managerId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {
            salaryService.delete(salaryService.getBySalaryId(UUID.fromString(deleteId)));
            redirectAttributes.addAttribute("user", userId);
            redirectAttributes.addAttribute("manager", managerId);
            redirectAttributes.addAttribute("year", year);
            redirectAttributes.addAttribute("month", month);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:infoSalary";
            } else {
                String redirectUrl = String.format("redirect:https://%s/infoSalary", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in deleteSalary(post)");
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
    
    // 講師削除（社員用）
    @GetMapping("/deleteUser")
    public String deleteUser(HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("user") String userId, @RequestParam("manager") String managerId, @RequestParam("year") String year, @RequestParam("month") String month) {
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
            userService.deleteById(UUID.fromString(userId));
            redirectAttributes.addAttribute("manager", managerId);
            String host = request.getHeader("Host");
            if (host.equals(domainLocal)) {
                return "redirect:indexManager";
            } else {
                String redirectUrl = String.format("redirect:https://%s/indexManager", domainAWS);
                return redirectUrl;
            }
        } catch (Exception e) {
            System.out.println("Error happened in deleteUser(get)");
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

    // 社員アカウント情報更新（社員用）
    @GetMapping("/editManagerForm")
    String editManagerFormGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("edit") String editId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(editId));
            model.addAttribute("manager", manager);
            model.addAttribute("managerUpdateForm", manager);
            return "editManagerForm";
        } catch (Exception e) {
            System.out.println("Error happened in editManagerForm.html");
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

    // 社員アカウント情報更新（社員用）
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
            System.out.println("Error happened in editManagerForm(post)");
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

    // 社員アカウント削除（社員用）
    @GetMapping("/deleteManager")
    String deleteManager(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, @RequestParam("delete") String deleteId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(deleteId));
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
    
    // 講師ログイン（講師用）
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
    
    // 社員ログイン（社員用）
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
    
    // 講師ログイン（講師用）
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
                Calendar calendar = Calendar.getInstance();
                String year = DateSet.getYear(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
                String month = DateSet.getMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH));
                redirectAttributes.addAttribute("user", userId);
                redirectAttributes.addAttribute("year", year);
                redirectAttributes.addAttribute("month", month);
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
    
    // 社員ログイン（社員用）
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
    
    // 講師アカウント作成（社員用）
    @GetMapping("/signUp")
    String signUpGet(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, @RequestParam("manager") String managerId) {
        try {
            Manager manager = managerService.getByManagerId(UUID.fromString(managerId));
            User user = new User();
            Salary salary = new Salary();
            user.setClassAreaId(manager.getId());
            model.addAttribute("manager", manager);
            model.addAttribute("userCreateForm", user);
            model.addAttribute("salaryCreateForm", salary);
            return "signUp";
        } catch (Exception e) {
            System.out.println("Error happened in signUp.html");
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
    
    // 社員アカウント作成（社員用）
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
    
    // 講師アカウント作成（社員用）
    @PostMapping("/signUp")
    String signUpPost(HttpServletRequest request, @ModelAttribute("userCreateForm") User user, @ModelAttribute("salaryCreateForm") Salary salary, RedirectAttributes redirectAttributes) {
        try {
            if (userService.getByLoginId(user.getLoginId()) == null) {
                userService.add(user);
                User setUser = userService.getByLoginId(user.getLoginId());
                salary.setUserId(setUser.getId());
                salaryService.add(salary);
                redirectAttributes.addAttribute("manager", user.getClassAreaId());
                String host = request.getHeader("Host");
                if (host.equals(domainLocal)) {
                    return "redirect:indexManager";
                } else {
                    String redirectUrl = String.format("redirect:https://%s/indexManager", domainAWS);
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
            System.out.println("Error happened in signUp(post)");
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
    
    // 社員アカウント作成（社員用）
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

    // Excelファイルのエクスポート
    @GetMapping("/downloadExcel")
    public void downloadExcel(HttpServletRequest request, HttpServletResponse response, @RequestParam("user") String userId, @RequestParam("year") String year, @RequestParam("month") String month) {
        try {

            // 講師情報・勤務情報
            User user = userService.getByUserId(UUID.fromString(userId));
            Manager manager = managerService.getByManagerId(user.getClassAreaId());
            Date dateFrom = DateSet.getDatePeriod(year, month)[0];
            Date dateTo = DateSet.getDatePeriod(year, month)[1];
            List<Work> workList = workService.findByUserId(user.getId(), dateFrom, dateTo);

            // レスポンスヘッダーの設定
            String fileName = URLEncoder.encode(user.getUserName().replace(" ", "")+"_"+year+"_"+month, StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"");
            
            // テンプレートファイルを読み込み
            ClassPathResource templateResource = new ClassPathResource("static/excel/excelTemplate.xlsx");
            InputStream templateInputStream = templateResource.getInputStream();
            Workbook workbook = new XSSFWorkbook(templateInputStream);

            // テンプレートのシートを取得
            Sheet sheet = workbook.getSheetAt(0);

            // シート名を変更
            String sheetName = user.getUserName().replace(" ", "")+"_"+year+"_"+month;
            workbook.setSheetName(workbook.getSheetIndex(sheet), sheetName);

            // テンプレートのセルにデータを埋め込む
            String headers[] = new String[8];
            int cellPointsInHeader[] = {4, 16, 25, 29, 33, 35, 40, 42};
            for (int i = 0; i < headers.length; i++) {
                headers[i] = "";
            }
            headers[0] = manager.getClassArea();
            headers[1] = user.getUserName();
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
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.getCell(cellPointsInHeader[i]);
                if (cell == null) {
                    cell = headerRow.createCell(cellPointsInHeader[i]);
                }
                if (i == 2 || i == 3 || i == 4 || i == 6) {
                    cell.setCellValue(Integer.valueOf(headers[i]));
                } else {
                    cell.setCellValue(headers[i]);
                }
            }
            int sumInt[] = new int[11];
            int cellPointsInSum[] = {1, 4, 7, 11, 15, 19, 23, 27, 31, 39, 42};
            for (int i = 0; i < sumInt.length; i++) {
                sumInt[i] = 0;
            }
            int rowIndex = 7;
            int cellPointsInWork[] = {1, 4, 5, 8, 10, 13, 15, 17, 19, 21, 23, 25, 27, 29, 33, 35, 37, 39, 41, 43, 45};
            for (Work work : workList) {
                String values[] = new String[21];
                for (int i = 0; i < values.length; i++) {
                    values[i] = "";
                }
                values[0] = work.getDate().substring(5,10).replace("-","月")+"日";
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
                    values[11] = Integer.toString(salaryService.getByDate(user.getId(), work.getDate()).getSupportSalary());
                }
                if (work.getCarfare() != 0) {
                    values[12] = Integer.toString(work.getCarfare());
                }
                if (!work.getOtherWork().equals("")) {
                    values[13] = work.getOtherWork();
                }
                if (!work.getOtherTimeStart().equals("     ")) {
                    if (Integer.valueOf(work.getOtherTimeStart().split(":")[0]) < 10) {
                        values[15] = Integer.toString(Integer.valueOf(work.getOtherTimeStart().split(":")[0])) + ":" + work.getOtherTimeStart().split(":")[1];
                    } else {
                        values[15] = work.getOtherTimeStart();
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
                    sumInt[3] += salaryService.getByDate(user.getId(), work.getDate()).getSupportSalary();
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
                for (int i = 0; i < cells.length; i++) {
                    cells[i] = row.getCell(cellPointsInWork[i]);
                    if (cells[i] == null) {
                        cells[i] = row.createCell(cellPointsInWork[i]);
                    }
                    if (i == 3 || i == 11 || i == 12) {
                        if (!values[i].equals("")) {
                            cells[i].setCellValue(Integer.valueOf(values[i]));
                        }
                    } else {
                        cells[i].setCellValue(values[i]);
                    }
                }
                if (!work.getTimeStart().equals("     ")) {
                    for (int i = 5; i < 8; i++) {
                        CellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setAlignment(HorizontalAlignment.CENTER);
                        style.setVerticalAlignment(VerticalAlignment.CENTER);
                        style.setBorderTop(BorderStyle.THIN);
                        style.setBorderBottom(BorderStyle.THIN);
                        if (i != 6) {
                            style.setBorderLeft(BorderStyle.THIN);
                        } else {
                            style.setBorderLeft(BorderStyle.DOTTED);
                        }
                        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
                        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                        cells[i].setCellStyle(style);
                    }
                }
                rowIndex += 1;
            }
            if (salaryService.getByDate(user.getId(), workList.get(0).getDate()).getCarfare() != 0) {
                sumInt[10] = salaryService.getByDate(user.getId(), workList.get(0).getDate()).getCarfare();
            }
            for (int i = 0; i < sumInt.length; i++) {
                if (sumInt[i] != 0) {
                    Row row = sheet.getRow(3);
                    if (row == null) {
                        row = sheet.createRow(3);
                    }
                    Cell cell = row.getCell(cellPointsInSum[i]);
                    if (cell == null) {
                        cell = row.createCell(cellPointsInSum[i]);
                    }
                    cell.setCellValue(sumInt[i]);
                }
            }

            // Excelデータをレスポンスに書き込む
            workbook.write(response.getOutputStream());
            workbook.close();
            response.getOutputStream().flush();

        } catch (Exception e) {
            // エラー処理（jsでリダイレクトさせる）
            e.printStackTrace();
        }
    }
    
}
