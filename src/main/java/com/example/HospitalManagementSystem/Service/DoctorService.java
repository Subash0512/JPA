package com.example.HospitalManagementSystem.Service;

import com.example.HospitalManagementSystem.Exception.BadRequestException;
import com.example.HospitalManagementSystem.Exception.ResourceNotFoundException;
import com.example.HospitalManagementSystem.Model.Doctor;
import com.example.HospitalManagementSystem.Repsitory.DoctorRepo;
import com.example.HospitalManagementSystem.Repsitory.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.HospitalManagementSystem.Model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

	private final DoctorRepo doctorRepo;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DoctorService(
	        DoctorRepo doctorRepo,
	        UserRepository userRepository,
	        PasswordEncoder passwordEncoder) {

	    this.doctorRepo = doctorRepo;
	    this.userRepository = userRepository;
	    this.passwordEncoder = passwordEncoder;
	}

    // =========================================================
    // CREATE DOCTOR
    // =========================================================

    public Doctor saveDoctor(Doctor doctor) {

        if (doctorRepo.findByEmail(doctor.getEmail()).isPresent()) {

            throw new BadRequestException(
                    "Doctor email already exists"
            );
        }

        if (doctorRepo.findByPhone(doctor.getPhone()).isPresent()) {

            throw new BadRequestException(
                    "Doctor phone number already exists"
            );
        }

        return doctorRepo.save(doctor);
    }


    // =========================================================
    // GET ALL DOCTORS
    // =========================================================

    public List<Doctor> getAllDoctors() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String role = getRole(authentication);


        // =====================================================
        // PATIENT → CAN SEE ALL DOCTORS
        // =====================================================

        if (role.equals("ROLE_PATIENT")) {

            return doctorRepo.findAll();
        }


        // =====================================================
        // ADMIN → CAN SEE ALL DOCTORS
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            return doctorRepo.findAll();
        }


        // =====================================================
        // DOCTOR → ONLY OWN PROFILE
        // =====================================================

        if (role.equals("ROLE_DOCTOR")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Doctors can only access their own profile"
            );
        }


        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access doctors"
        );
    }


    // =========================================================
    // GET DOCTOR BY ID
    // =========================================================

    public Optional<Doctor> getDoctorById(Long id) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String role = getRole(authentication);


        // =====================================================
        // ADMIN → ANY DOCTOR
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            return doctorRepo.findById(id);
        }


        // =====================================================
        // PATIENT → ANY DOCTOR
        // =====================================================

        if (role.equals("ROLE_PATIENT")) {

            return doctorRepo.findById(id);
        }


        // =====================================================
        // DOCTOR → ONLY OWN PROFILE
        // =====================================================

        if (role.equals("ROLE_DOCTOR")) {

            String username =
                    authentication.getName();

            Doctor loggedInDoctor =
                    doctorRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Doctor account not found"
                                    )
                            );


            if (!loggedInDoctor
                    .getId()
                    .equals(id)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to access another doctor's profile"
                );
            }


            return Optional.of(loggedInDoctor);
        }


        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to access doctors"
        );
    }


    // =========================================================
    // GET LOGGED-IN DOCTOR PROFILE
    // =========================================================

    public Doctor getMyProfile() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String role = getRole(authentication);


        if (!role.equals("ROLE_DOCTOR")) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can access this profile"
            );
        }


        String username =
                authentication.getName();


        return doctorRepo
                .findByUserUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor account not found"
                        )
                );
    }


    // =========================================================
    // UPDATE DOCTOR
    // =========================================================

    public Doctor updateDoctor(
            Long id,
            Doctor updatedDoctor) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String role = getRole(authentication);


        Doctor existingDoctor =
                doctorRepo.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Doctor not found with ID: " + id
                                )
                        );


        // =====================================================
        // ADMIN → CAN UPDATE ANY DOCTOR
        // =====================================================

        if (role.equals("ROLE_ADMIN")) {

            // Allowed
        }


        // =====================================================
        // DOCTOR → CAN UPDATE ONLY OWN PROFILE
        // =====================================================

        else if (role.equals("ROLE_DOCTOR")) {

            String username =
                    authentication.getName();

            Doctor loggedInDoctor =
                    doctorRepo
                            .findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Doctor account not found"
                                    )
                            );


            if (!loggedInDoctor
                    .getId()
                    .equals(id)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You are not authorized to update another doctor's profile"
                );
            }
        }


        // =====================================================
        // PATIENT / OTHER ROLES → NOT ALLOWED
        // =====================================================

        else {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to update doctor profiles"
            );
        }


        // =====================================================
        // DUPLICATE EMAIL CHECK
        // =====================================================

        Optional<Doctor> doctorWithEmail =
                doctorRepo.findByEmail(
                        updatedDoctor.getEmail()
                );


        if (doctorWithEmail.isPresent()
                && !doctorWithEmail
                        .get()
                        .getId()
                        .equals(id)) {

            throw new BadRequestException(
                    "Doctor email already exists"
            );
        }


        // =====================================================
        // DUPLICATE PHONE CHECK
        // =====================================================

        Optional<Doctor> doctorWithPhone =
                doctorRepo.findByPhone(
                        updatedDoctor.getPhone()
                );


        if (doctorWithPhone.isPresent()
                && !doctorWithPhone
                        .get()
                        .getId()
                        .equals(id)) {

            throw new BadRequestException(
                    "Doctor phone number already exists"
            );
        }


        // =====================================================
        // UPDATE PROFILE FIELDS
        // =====================================================

        existingDoctor.setName(
                updatedDoctor.getName()
        );

        existingDoctor.setSpecialization(
                updatedDoctor.getSpecialization()
        );

        existingDoctor.setEmail(
                updatedDoctor.getEmail()
        );

        existingDoctor.setPhone(
                updatedDoctor.getPhone()
        );


        /*
         * IMPORTANT:
         *
         * Do NOT update the User relationship here.
         *
         * The authenticated doctor's account must remain
         * associated with the same Doctor entity.
         */

        return doctorRepo.save(existingDoctor);
    }

 // =========================================================
 // DELETE DOCTOR
 // =========================================================

    @Transactional
    public void deleteDoctor(Long id) {
     Authentication authentication =
             SecurityContextHolder
                     .getContext()
                     .getAuthentication();

     String role = getRole(authentication);

     // =====================================================
     // ONLY ADMIN CAN DELETE DOCTORS
     // =====================================================

     if (!role.equals("ROLE_ADMIN")) {

         throw new ResponseStatusException(
                 HttpStatus.FORBIDDEN,
                 "Only admin can delete doctors"
         );
     }

     // =====================================================
     // FIND DOCTOR
     // =====================================================

     Doctor doctor =
             doctorRepo.findById(id)
                     .orElseThrow(() ->
                             new ResourceNotFoundException(
                                     "Doctor not found with ID: " + id
                             )
                     );

     // =====================================================
     // DELETE LINKED USER ACCOUNT FIRST
     // =====================================================
     //
     // A doctor may have a User login account.
     // Delete the User before deleting the Doctor so
     // no orphaned doctor login remains.
     // =====================================================

     if (doctor.getUser() != null) {

         userRepository.delete(doctor.getUser());
     }

     // =====================================================
     // DELETE DOCTOR
     // =====================================================

     doctorRepo.delete(doctor);
 }
    // =========================================================
    // FIND BY EMAIL
    // =========================================================

    public Optional<Doctor> findByEmail(String email) {

        return doctorRepo.findByEmail(email);
    }


    // =========================================================
    // FIND BY SPECIALIZATION
    // =========================================================

    public List<Doctor> findBySpecialization(
            String specialization) {

        return doctorRepo.findBySpecialization(
                specialization
        );
    }


    // =========================================================
    // FIND BY PHONE
    // =========================================================

    public Optional<Doctor> findByPhone(String phone) {

        return doctorRepo.findByPhone(phone);
    }


    // =========================================================
    // GET CURRENT USER ROLE
    // =========================================================

    private String getRole(
            Authentication authentication) {

        if (authentication == null) {

            return "";
        }


        return authentication
                .getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority())
                .orElse("");
    }
    
 // =========================================================
 // CREATE DOCTOR + LOGIN
 // =========================================================

 @Transactional
 public Doctor createDoctorWithLogin(
         Doctor doctor,
         String username,
         String password) {

     if (doctor == null) {
         throw new BadRequestException(
                 "Doctor details are required"
         );
     }

     if (username == null || username.trim().isEmpty()) {
         throw new BadRequestException(
                 "Doctor username is required"
         );
     }

     if (password == null || password.length() < 6) {
         throw new BadRequestException(
                 "Doctor password must be at least 6 characters"
         );
     }

     String normalizedUsername =
             username.trim();

     /*
      * Username must be unique.
      */
     if (userRepository.existsByUsername(normalizedUsername)) {
         throw new BadRequestException(
                 "Username already exists"
         );
     }

     /*
      * Doctor email must be unique.
      */
     if (doctor.getEmail() != null &&
             doctorRepo.findByEmail(
                     doctor.getEmail().trim()
             ).isPresent()) {

         throw new BadRequestException(
                 "Doctor email already exists"
         );
     }

     /*
      * Doctor phone must be unique.
      */
     if (doctor.getPhone() != null &&
             doctorRepo.findByPhone(
                     doctor.getPhone().trim()
             ).isPresent()) {

         throw new BadRequestException(
                 "Doctor phone number already exists"
         );
     }

     /*
      * Save doctor first.
      */
     Doctor savedDoctor =
             doctorRepo.save(doctor);

     /*
      * Create login account.
      */
     User user = new User();

     user.setUsername(
             normalizedUsername
     );

     user.setPassword(
             passwordEncoder.encode(password)
     );

     user.setRole("DOCTOR");

     user.setEnabled(true);

     User savedUser =
             userRepository.save(user);

     /*
      * Link login account to doctor.
      */
     savedDoctor.setUser(savedUser);

     return doctorRepo.save(savedDoctor);
 }
}