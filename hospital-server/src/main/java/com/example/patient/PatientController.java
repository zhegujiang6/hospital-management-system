package com.example.patient;



import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {


    private final PatientService patientService;

    public PatientController(PatientService patientService) {

        this.patientService = patientService;
    }

    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody PatientCreateRequest request) {
        Long id = patientService.create(request);
        return Result.success(id);
    }

    @GetMapping
    public Result<List<Patient>> list() {
        List<Patient> patients = patientService.listPatients();
        return Result.success(patients);
    }

    @GetMapping("/{id}")
    public Result<Patient> getDetail(@PathVariable Long id) {
        Patient patient = patientService.getDetail(id);
        return Result.success(patient);
    }

    @PutMapping("/{id}")
    @Valid
    public Result<Void> update(@PathVariable Long id, @RequestBody PatientUpdateRequest request) {
        patientService.update(id, request);

        return Result.success(null);
    }


    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return Result.success(null);
    }


}
