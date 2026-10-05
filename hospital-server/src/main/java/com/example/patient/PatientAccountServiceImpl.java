package com.example.patient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.auth.SysUser;
import com.example.auth.UserRole;
import com.example.exception.BusinessException;
import com.example.auth.SysUserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class PatientAccountServiceImpl
        implements PatientAccountService {

    private final PatientService patientService;

    private final SysUserMapper sysUserMapper;

    private final PasswordEncoder passwordEncoder;

    public PatientAccountServiceImpl(
            PatientService patientService,
            SysUserMapper sysUserMapper,
            PasswordEncoder passwordEncoder) {

        this.patientService = patientService;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 校验患者和用户名，并使用BCrypt保存患者登录账号。
     */
    @Override
    public Long create(
            PatientAccountCreateRequest request) {

        Patient patient =
                patientService.getDetail(
                        request.getPatientId()
                );

        if (!Integer.valueOf(1).equals(
                patient.getStatus())) {

            throw new BusinessException(
                    "患者档案已停用"
            );
        }

        /*
         * 用户名统一转成小写。
         * 避免Patient01和patient01被当作两个不同账号。
         */
        String username =
                request.getUsername()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        SysUser usernameUser =
                sysUserMapper.selectByUsername(
                        username
                );

        if (usernameUser != null) {
            throw new BusinessException(
                    "用户名已存在"
            );
        }

        /*
         * 一个患者档案只能绑定一个登录账号。
         */
        LambdaQueryWrapper<SysUser> patientWrapper =
                new LambdaQueryWrapper<>();

        patientWrapper.eq(
                SysUser::getPatientId,
                request.getPatientId()
        );

        boolean patientBound =
                sysUserMapper.exists(
                        patientWrapper
                );

        if (patientBound) {
            throw new BusinessException(
                    "该患者已经绑定登录账号"
            );
        }

        SysUser sysUser = new SysUser();

        sysUser.setUsername(username);

        /*
         * 绝对不能保存明文密码。
         * BCrypt每次生成的密文通常不同，但都可以验证原密码。
         */
        sysUser.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        sysUser.setRealName(
                patient.getName()
        );

        sysUser.setRole(
                UserRole.PATIENT.name()
        );

        sysUser.setDoctorId(null);

        sysUser.setPatientId(
                patient.getId()
        );

        sysUser.setStatus(1);

        try {
            int inserted =
                    sysUserMapper.insert(sysUser);

            if (inserted != 1) {
                throw new BusinessException(
                        "患者账号创建失败"
                );
            }
        } catch (DuplicateKeyException exception) {

            /*
             * 防止并发请求同时使用相同用户名，
             * 或同时为同一个患者创建账号。
             */
            throw new BusinessException(
                    "用户名已存在或患者已绑定账号"
            );
        }

        return sysUser.getId();
    }
}