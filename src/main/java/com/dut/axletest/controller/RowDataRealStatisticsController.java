package com.dut.axletest.controller;

import com.dut.axletest.datssource.DataMap;
import com.dut.axletest.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class RowDataRealStatisticsController {

    @Autowired
    DataMap dataMap;

    @RequestMapping("/message/init/rowdata/{id}")
    @ResponseBody
    public List<Message> initDynamicRowDataRealStatistics(@PathVariable Long id){
        return dataMap.getInitAllMessage(id);

    }

    @RequestMapping("/message/increment/rowdata/{id}")
    @ResponseBody
    public List<Message> incrementDynamicRowDataRealStatistics(@PathVariable Long id){
        return dataMap.getIncreamMessage(id);
    }
}