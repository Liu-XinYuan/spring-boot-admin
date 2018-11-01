package com.test.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.test.mysql.entity.*;
import com.test.mysql.model.ElectronicDataForReportQo;
import com.test.mysql.repository.DepartmentRepository;
import com.test.mysql.repository.ElectronicDataForReportRepository;
import com.test.mysql.repository.ReportCollectRepository;
import com.test.web.Utils.DateUtil;
import com.test.web.config.CustomSecurityMetadataSource;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.*;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.out;

/**
 * Created by dongmingjun on 2017/5/15.
 */
@Controller
@RequestMapping("/reportCollect")
public class ReportCollectController {
    private static Logger logger = LoggerFactory.getLogger(ReportCollectController.class);
    private SimpleDateFormat formatFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ReportCollectRepository reportCollectRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ElectronicDataForReportRepository reportRepository;

    @RequestMapping("/index")
    public String index(ModelMap model, Principal user) throws Exception {
        Authentication authentication = (Authentication) user;
        List<String> userroles = new ArrayList<String>();
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            userroles.add(ga.getAuthority());
        }

        boolean newrole = false, editrole = false, deleterole = false;
        for (String key : CustomSecurityMetadataSource.resourceMap.keySet()) {
            if (key.contains("new")) {
                for (ConfigAttribute ca : CustomSecurityMetadataSource.resourceMap.get(key)) {
                    if (userroles.contains(ca.getAttribute())) {
                        newrole = true;
                        break;
                    }
                }

            }
            if (key.contains("edit")) {
                for (ConfigAttribute ca : CustomSecurityMetadataSource.resourceMap.get(key)) {
                    if (userroles.contains(ca.getAttribute())) {
                        editrole = true;
                        break;
                    }
                }

            }
            if (key.contains("delete")) {
                for (ConfigAttribute ca : CustomSecurityMetadataSource.resourceMap.get(key)) {
                    if (userroles.contains(ca.getAttribute())) {
                        deleterole = true;
                        break;
                    }
                }

            }
        }

        out.print("new role is" + newrole + "editrole is " + editrole + "deleterole is " + deleterole);
        model.addAttribute("newrole", newrole);
        model.addAttribute("editrole", editrole);
        model.addAttribute("deleterole", deleterole);

        model.addAttribute("user", user);
        logger.info("汇总报表页面被访问到");
        return "reportCollect/index";
    }


    @RequestMapping(value = "/list")
    @ResponseBody
    public Page<F_garbageCollect> getList(ElectronicDataForReportQo electronicDataForReportQo) {
        Pageable pageable = new PageRequest(electronicDataForReportQo.getPage(), electronicDataForReportQo.getSize(), new Sort(Sort.Direction.ASC, "id"));
        Date start = DateUtil.getTime(-1, 0, 0, 0);
        Date end = DateUtil.getTime(0, 0, 0, 0);
        Page<F_garbageCollect> list = null;
        try {
            if (electronicDataForReportQo.getStart() == null || electronicDataForReportQo.getStart() == "") {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -30);
                start = cal.getTime();
            } else {
                out.print(electronicDataForReportQo.getStart() + "开始..........................");
                start = formatFull.parse(electronicDataForReportQo.getStart() + ":00");
            }


            if (electronicDataForReportQo.getEnd() == null || electronicDataForReportQo.getEnd() == "") {
                end = new Date();
            } else {
                out.print(electronicDataForReportQo.getEnd() + "结束..........................");
                end = formatFull.parse(electronicDataForReportQo.getEnd() + ":59");
            }
            list = reportCollectRepository.findByTime(start, end, pageable);
            out.print(list.iterator().hasNext());
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage() + "解析日期出现错误");

        }
        return null;
    }

    @RequestMapping("/view")
    public String view(ModelMap model, ElectronicDataForReportQo electronicDataForReportQo) {
        out.println("收到图表页面请求");
        return "reportCollect/view";


    }


    public List<F_garbageCollect> getAllList(ElectronicDataForReportQo electronicDataForReportQo) {
        Pageable pageable = new PageRequest(electronicDataForReportQo.getPage(), electronicDataForReportQo.getSize(), new Sort(Sort.Direction.ASC, "id"));
        List<F_garbageCollect> list = new ArrayList<F_garbageCollect>();
        Date start = DateUtil.getTime(-1, 0, 0, 0);
        Date end = DateUtil.getTime(0, 0, 0, 0);
        try {
            if (electronicDataForReportQo.getStart() == null || electronicDataForReportQo.getStart() == "") {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -30);
                start = cal.getTime();
            } else {
                start = formatFull.parse(electronicDataForReportQo.getStart() + ":00");
            }


            if (electronicDataForReportQo.getEnd() == null || electronicDataForReportQo.getEnd() == "") {
                end = new Date();
            } else {
                end = formatFull.parse(electronicDataForReportQo.getEnd() + ":59");
            }
            list = reportCollectRepository.findAll(start, end);
            return list;
        } catch (Exception e) {
            logger.error(e.getMessage() + "解析日期出现错误");

        }
        return null;
    }

    @RequestMapping(value = "/department")
    @ResponseBody
    public JSONArray viewDepartment(ElectronicDataForReportQo electronicDataForReportQo) {
        //获取部门列表
        List<Department> departments = departmentRepository.findAll();
        JSONArray array = new JSONArray();

        for (int i = 0, len = departments.size(); i < len; i++) {
            if (!"it".equalsIgnoreCase(departments.get(i).getName())) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("depar", departments.get(i).getName());
                array.add(jsonObject);
            }
        }
        out.print("获取部门列表成功：" + array.size());
        return array;
    }

    @RequestMapping(value = "/netweight")
    @ResponseBody
    public JSONArray viewNetWeight(ElectronicDataForReportQo electronicDataForReportQo) {

        Iterator<F_garbageCollect> netWeightList = this.getAllList(electronicDataForReportQo).iterator();
        JSONArray array = new JSONArray();
        F_garbageCollect fg = new F_garbageCollect();
        while (netWeightList.hasNext()) {
            fg = netWeightList.next();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("categoryName", fg.getCategoryName());
            jsonObject.put("netWeight", fg.getNetWeight());
            jsonObject.put("depar", fg.getDepartment());
            array.add(jsonObject);
        }
        return array;
    }


    /**
     * 文件下载
     *
     * @param fileName
     * @param request
     * @param response
     * @return
     * @Description:
     */
    @RequestMapping("/exportExcel")
    @ResponseBody
    public ResponseEntity<byte[]> downloadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ElectronicDataForReportQo electronicDataForReportQo = new ElectronicDataForReportQo();
        electronicDataForReportQo.setStart(request.getParameter("start"));
        electronicDataForReportQo.setEnd(request.getParameter("end"));
        //拿到净重数据
        List<F_garbageCollect> netWeightList = this.getAllList(electronicDataForReportQo);
        //获取部门列表
        List<Department> departments = departmentRepository.findAll();
        //构建部门和操作员map
        Map<String, String> deptOperaMap = buildMap(electronicDataForReportQo);

        //获取垃圾类型
        String[] garbageType = new String[]{"感染性废物", "病理性废物", "损伤性废物", "药物性废物", "化学性废物", "其他废物"};
        //创建workbook
        HSSFWorkbook workbook = new HSSFWorkbook();
        String sheetName = "医疗废物院内交接登记表";
        //单元格样式
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中
        //添加Worksheet（不添加sheet时生成的xls文件打开时会报错)
        HSSFSheet sheet1 = workbook.createSheet(sheetName);
        HSSFCell cell = null;
        HSSFRow row = null;
        //创建头部表名称
        CellRangeAddress cra = new CellRangeAddress(0, 1, 0, 10 + 2 * garbageType.length);
        sheet1.addMergedRegion(cra);
        row = sheet1.createRow(0);
        cell = row.createCell(0);
        cell.setCellValue("医疗废物院内交接登记表");
        cell.getCellStyle().setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);


        //添加日期栏
        cra = new CellRangeAddress(2, 2, 0, 10 + 2 * garbageType.length);
        sheet1.addMergedRegion(cra);
        row = sheet1.createRow(2);
        cell = row.createCell(0);
        String now = formatFull.format(new Date());
        cell.setCellValue("日期  年：" + now.substring(0, 4) + " 月：" + now.substring(5, 7) + " 日：" + now.substring(8, 10));

        //添加项目科室
        row = sheet1.createRow(3);
        for (int i = 0; i <= 5 + garbageType.length; i += (5 + garbageType.length)) {
            sheet1.addMergedRegion(new CellRangeAddress(3, 4, i, i));
            cell = row.createCell(i);
            cell.setCellValue("科室\\项目");
        }


        //添加废物标识
        for (int i = 1; i <= (6 + garbageType.length); i += (5 + garbageType.length)) {
            sheet1.addMergedRegion(new CellRangeAddress(3, 3, i, i + garbageType.length - 1));
            cell = row.createCell(i);
            cell.setCellValue("医疗废物种类,重量(KG)");
        }


        //添加废物名称
        row = sheet1.createRow(4);
        for (short i = 1; i < garbageType.length + 1; i++) {
            for (short j = 0; j <= (5 + garbageType.length); j += (5 + garbageType.length)) {
                cell = row.createCell(i + j);
                cell.setCellValue(garbageType[i - 1]);
            }
        }

        //创建时间签名栏
        String[] signs = new String[]{"交接时间", "科室移交签名", "专职运送签名", "医疗废物最终去向"};
        row = sheet1.getRow(3);
        for (short i = 0; i < signs.length; i++) {
            for (short j = 0; j <= (5 + garbageType.length); j += (5 + garbageType.length)) {
                cra = new CellRangeAddress(3, 4, i + j + 1 + garbageType.length, i + j + 1 + garbageType.length);
                sheet1.addMergedRegion(cra);
                cell = row.createCell(i + j + 1 + garbageType.length);
                cell.setCellValue(signs[i].toString());
            }
        }

        //左边数据条数
        int leftResordes = (int) Math.ceil((double) departments.size() / (double) 2);

        //存入左边数据
        for (short i = 5; i < leftResordes + 5; i++) {   //行索引
            //创建第i行
            row = sheet1.createRow(i);
            for (short j = 0; j < garbageType.length + 1; j++) {//列索引
                cell = row.createCell(j);
                if (j <= 0) {
                    //第一列为部门
                    cell.setCellValue(departments.get(i - 5).getName());
                } else {
                    //其它列为净重
                    cell.setCellValue(this.getBydeptAndGarbageType(netWeightList, departments.get(i - 5).getName(), garbageType[j - 1].toString()));
                }
            }
        }

        //存入右边数据
        for (int i = (5 + leftResordes); i < departments.size() + 5; i++) {   //行索引
            row = sheet1.getRow(i - leftResordes);
            for (short j = 0; j < garbageType.length + 1; j++) {//列索引
                cell = row.createCell(j + 5 + garbageType.length);
                if (j <= 0) {
                    //第一列为部门
                    cell.setCellValue(departments.get(i - 5).getName());
                } else {
                    //其它列为净重
                    cell.setCellValue(this.getBydeptAndGarbageType(netWeightList, departments.get(i - 5).getName(), garbageType[j - 1].toString()));
                }
            }
        }

        //交接时间填充
        for (int i = 0; i <= 5 + garbageType.length; i += (garbageType.length + 5)) {
            for (int j = 1; j <= leftResordes; j++) {
                row = sheet1.getRow(4 + j) == null ? sheet1.createRow(4 + j) : sheet1.getRow(4 + j);
                cell = row.createCell(i + garbageType.length + 1);
                cell.setCellValue(now);
            }
        }

        //操作员填充
        for (int i = 0; i <= 5 + garbageType.length; i += (garbageType.length + 5)) {
            for (int j = 1; j <= leftResordes; j++) {
                row = sheet1.getRow(4 + j) == null ? sheet1.createRow(4 + j) : sheet1.getRow(4 + j);
                cell = row.createCell(i + garbageType.length + 2);
                String value = null;
                if (i == 0) {
                    value = row.getCell(0) != null ? deptOperaMap.get(row.getCell(0).getStringCellValue()) : null;

                } else {
                    value = row.getCell(5 + garbageType.length) != null ? deptOperaMap.get(row.getCell(5 + garbageType.length).getStringCellValue()) : null;
                }
                cell.setCellValue(value == null ? "" : value);

            }
        }

        //废物去向说明
        for (short i = 0; i <= (5 + garbageType.length); i += (5 + garbageType.length)) {
            cra = new CellRangeAddress(5, 5 + leftResordes, 4 + garbageType.length + i, 4 + garbageType.length + i);
            sheet1.addMergedRegion(cra);

            row = sheet1.getRow(5) == null ? sheet1.createRow(5) : sheet1.getRow(5);
            cell = row.createCell(4 + garbageType.length + i);
            cell.setCellValue("无锡市工业医疗安全处置有限公司");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
        } catch (IOException e) {
            logger.error("excel写入失败");
        }

        HttpHeaders headers = new HttpHeaders();
        String fileName = new String("医疗废物院内交接登记表.xls".getBytes("UTF-8"), "iso-8859-1");//为了解决中文名称乱码问题
        headers.setContentDispositionFormData("attachment", fileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ResponseEntity<byte[]> filebyte = new ResponseEntity<byte[]>(out.toByteArray(), headers, HttpStatus.CREATED);
        try {
            out.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return filebyte;
    }


    //通过科室和垃圾类型查询净重
    private Double getBydeptAndGarbageType(List<F_garbageCollect> f, String dept, String type) {
        Double net;
        F_garbageCollect fc;
        Iterator<F_garbageCollect> it = f.iterator();
        //dept传值没有错
        while (it.hasNext()) {
            fc = it.next();
            out.println("调用净重函数" + dept + "比较" + fc.getDepartment().trim());
            if (fc.getDepartment().trim().equals(dept.trim()) && fc.getCategoryName().trim().equals(type.trim())) {
                net = fc.getNetWeight();
                return net;
            }
        }
        return 0.0;
    }

    public Map<String, String> buildMap(ElectronicDataForReportQo electronicDataForReportQo) {
        try {
            Date start = DateUtil.getTime(-1, 0, 0, 0);
            Date end = DateUtil.getTime(0, 0, 0, 0);
            if (electronicDataForReportQo.getStart() == null || electronicDataForReportQo.getStart() == "") {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -30);
                start = cal.getTime();
            } else {
                start = formatFull.parse(electronicDataForReportQo.getStart() + ":00");
            }


            if (electronicDataForReportQo.getEnd() == null || electronicDataForReportQo.getEnd() == "") {
                end = new Date();
            } else {
                end = formatFull.parse(electronicDataForReportQo.getEnd() + ":59");
            }


            List<Object[]> list = reportRepository.findBy2Fields(start,end);
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                Object[] m = list.get(i);
                String dept = m[0].toString();
                if (map.get(dept) == null) {
                    map.put(dept, list.get(i)[1].toString());
                } else {
                    String operator = map.get(dept) + ',' + list.get(i)[1];
                    map.put(dept, operator);
                }
            }
            return map;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

    }

}


