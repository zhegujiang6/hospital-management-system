package com.example.medicine;


import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    @Override
    public Long create(MedicineCreateRequest request){
        String medicineNo = request.getMedicineNo().trim().toUpperCase();

        boolean MedicineExits = lambdaQuery().eq(Medicine::getMedicineNo,medicineNo).exists();

        if(MedicineExits){
            throw new BusinessException("药品编号已存在");
        }
        Medicine medicine = new Medicine();

        medicine.setMedicineNo(medicineNo);
        medicine.setName(request.getName().trim());
        medicine.setGenericName(request.getGenericName());
        medicine.setCategory(request.getCategory());
        medicine.setDosageForm(request.getDosageForm());
        medicine.setSpecification(request.getSpecification());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setUnit(request.getUnit());
        medicine.setPrice(request.getPrice());
        medicine.setStockQuantity(request.getStockQuantity());
        medicine.setStockWarning(request.getStockWarning());
        medicine.setStatus(request.getStatus());
        medicine.setRemark(request.getRemark());


        save(medicine);
        return medicine.getId();
    }

}
