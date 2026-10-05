package com.example.department;

import com.example.common.PageResult;
import com.example.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public Result<PageResult<Department>> list(
            @RequestParam(
                    defaultValue = "1"
            ) long pageNo,

            @RequestParam(
                    defaultValue = "10"
            ) long pageSize,

            @RequestParam(
                    required = false
            ) String keyword,

            @RequestParam(
                    required = false
            ) Integer status) {

        PageResult<Department> pageResult =
                departmentService.pageQuery(
                        pageNo,
                        pageSize,
                        keyword,
                        status
                );

        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<Department> getDetail(@PathVariable Long id) {
        return Result.success(departmentService.getDetail(id));
    }

    @PostMapping
    public Result<Long> create(
            @Valid @RequestBody DepartmentCreateRequest request) {

        return Result.success(departmentService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {

        departmentService.update(id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success(null);
    }
}