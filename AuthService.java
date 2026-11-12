package cn.edu.sdua.qingshaonian.service;

import cn.edu.sdua.qingshaonian.config.JwtProvider;
import cn.edu.sdua.qingshaonian.dto.ApiException;
import cn.edu.sdua.qingshaonian.dto.Dtos;
import cn.edu.sdua.qingshaonian.entity.Role;
import cn.edu.sdua.qingshaonian.entity.User;
import cn.edu.sdua.qingshaonian.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 注册、登录与账号管理服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public Map<String, Object> register(Dtos.RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException("该账号已被注册");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(encoder.encode(req.password()));
        user.setRole(Role.STUDENT);
        user.setNickname(req.nickname() == null || req.nickname().isBlank() ? req.username() : req.nickname());
        user.setPhone(req.phone());
        user.setSchool(req.school());
        user.setGrade(req.grade());
        user.setClassName(req.className());
        user.setPsychProfileNo("PA" + LocalDateTime.now().toString().replaceAll("\\D", "").substring(0, 10));
        user.setStatus("ACTIVE");
        userRepository.save(user);
        return login(new Dtos.LoginRequest(req.username(), req.password()));
    }

    public Map<String, Object> login(Dtos.LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ApiException("账号或密码错误"));
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ApiException("账号已被停用，请联系管理员");
        }
        if (!encoder.matches(req.password(), user.getPassword())) {
            throw new ApiException("账号或密码错误");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtProvider.generateToken(user.getId(), user.getUsername(), user.getRole()));
        result.put("user", Dtos.sanitize(user));
        return result;
    }

    public User me(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("用户不存在"));
        return Dtos.sanitize(user);
    }

    @Transactional
    public User updateProfile(Long userId, Map<String, String> fields) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("用户不存在"));
        if (fields.containsKey("nickname") && fields.get("nickname") != null) {
            user.setNickname(fields.get("nickname"));
        }
        if (fields.containsKey("phone") && fields.get("phone") != null) {
            user.setPhone(fields.get("phone"));
        }
        if (fields.containsKey("school")) {
            user.setSchool(fields.get("school"));
        }
        if (fields.containsKey("grade")) {
            user.setGrade(fields.get("grade"));
        }
        if (fields.containsKey("className")) {
            user.setClassName(fields.get("className"));
        }
        if (Role.COUNSELOR.equals(user.getRole())) {
            if (fields.containsKey("title")) {
                user.setTitle(fields.get("title"));
            }
            if (fields.containsKey("specialty")) {
                user.setSpecialty(fields.get("specialty"));
            }
            if (fields.containsKey("intro")) {
                user.setIntro(fields.get("intro"));
            }
        }
        return Dtos.sanitize(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, Dtos.PasswordRequest req) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException("用户不存在"));
        if (!encoder.matches(req.oldPassword(), user.getPassword())) {
            throw new ApiException("原密码不正确");
        }
        user.setPassword(encoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    /** 管理员创建咨询师账号 */
    @Transactional
    public User createCounselor(Dtos.CounselorRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException("该账号已存在");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(encoder.encode(req.password()));
        user.setRole(Role.COUNSELOR);
        user.setNickname(req.nickname() == null || req.nickname().isBlank() ? req.username() : req.nickname());
        user.setPhone(req.phone());
        user.setTitle(req.title());
        user.setSpecialty(req.specialty());
        user.setLicenseNo(req.licenseNo());
        user.setIntro(req.intro());
        user.setPermissions(req.permissions() == null ? "case:view,case:edit,session:view" : req.permissions());
        user.setStatus("ACTIVE");
        userRepository.save(user);
        return Dtos.sanitize(user);
    }

    @Transactional
    public User updateUserByAdmin(Long id, Dtos.UserStatusRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new ApiException("用户不存在"));
        if (req.status() != null && !req.status().isBlank()) {
            if (Role.ADMIN.equals(user.getRole()) && !"ACTIVE".equals(req.status())) {
                throw new ApiException("不能停用管理员账号");
            }
            user.setStatus(req.status());
        }
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(encoder.encode(req.password()));
        }
        if (req.permissions() != null) {
            user.setPermissions(req.permissions());
        }
        return Dtos.sanitize(userRepository.save(user));
    }
}
