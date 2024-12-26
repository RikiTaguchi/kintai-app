package com.meikokintai.kintai_app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.meikokintai.kintai_app.model.IncomeTax;
import com.meikokintai.kintai_app.repository.IncomeTaxRepository;

@Service
public class IncomeTaxService {
    
    private final IncomeTaxRepository incomeTaxRepository;
    
    public IncomeTaxService(IncomeTaxRepository incomeTaxRepository) {
        this.incomeTaxRepository = incomeTaxRepository;
    }
    
    public List<IncomeTax> findAll() {
        return this.incomeTaxRepository.findAll();
    }

    public IncomeTax getByTotalIncome(int totalIncome) {
        return this.incomeTaxRepository.getByTotalIncome(totalIncome);
    }
    
}
