package com.example.medicine;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineCreateRequest {

    @NotBlank(message = "药品编号不能为空")
    @Size(max = 30, message = "药品编号不能超过30个字符")
    private String medicineNo;

    @NotBlank(message = "药品名称不能为空")
    @Size(max = 100, message = "药品名称不能超过30个字符")
    private String name;

    private String genericName;

    @NotBlank(message = "请选择药品分类")
    @Pattern(
            regexp = "ANTIBIOTIC|ANTIVIRAL|ANALGESIC|DIGESTIVE|CARDIOVASCULAR|OTHER",
            message = "药品分类不正确"
    )
    private String category;

    private String dosageForm;

    @NotBlank(message = "规格不能为空")
    private String specification;

    private String manufacturer;

    @NotNull(message = "单位不能为空")
    private String unit;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "单价必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stockQuantity;

    @NotNull(message = "库存预警值不能为空")
    @Min(value = 0, message = "库存预警值不能小于0")
    private Integer stockWarning;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;

    private String remark;

    public String getMedicineNo() {
        return medicineNo;
    }

    public void setMedicineNo(String medicineNo) {
        this.medicineNo = medicineNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getStockWarning() {
        return stockWarning;
    }

    public void setStockWarning(Integer stockWarning) {
        this.stockWarning = stockWarning;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
