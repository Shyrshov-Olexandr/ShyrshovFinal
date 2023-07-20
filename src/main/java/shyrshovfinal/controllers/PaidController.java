package internfinalproject.controllers;

import internfinalproject.Filter.PaidFilter;
import internfinalproject.models.*;
import internfinalproject.pojo.MessageResponse;
import internfinalproject.repository.CommentRepository;
import internfinalproject.repository.GroupRepository;
import internfinalproject.repository.PaidRepository;
import internfinalproject.repository.UserRepository;
import internfinalproject.service.UserDetailsImpl;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Api(value = "Paid controller")

@RestController
@RequestMapping("/api/paid")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaidController {
    @Autowired
    PaidRepository paidRepository;
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    UserRepository userRepository;
    @PersistenceContext
    private EntityManager em;
    @Autowired
    CommentRepository commentRepository;

    @Operation(summary = "get all paid",
            operationId = "getAllPaid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Paid.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @GetMapping("")
    @PreAuthorize("#user.is_active")
    @SecurityRequirement(name = "JWT")
    @Parameters({
            @Parameter(name = "page", description = "A page number within the paginated result set.", in = ParameterIn.QUERY),
            @Parameter(name = "id", in = ParameterIn.QUERY),
            @Parameter(name = "course", in = ParameterIn.QUERY),
            @Parameter(name = "name", in = ParameterIn.QUERY),
            @Parameter(name = "surname", in = ParameterIn.QUERY),
            @Parameter(name = "email", in = ParameterIn.QUERY),
            @Parameter(name = "phone", in = ParameterIn.QUERY),
            @Parameter(name = "age", in = ParameterIn.QUERY),
            @Parameter(name = "group", in = ParameterIn.QUERY),
            @Parameter(name = "courseFormat", in = ParameterIn.QUERY),
            @Parameter(name = "courseType", in = ParameterIn.QUERY),
            @Parameter(name = "startDate", in = ParameterIn.QUERY),
            @Parameter(name = "endDate", in = ParameterIn.QUERY),
            @Parameter(name = "status", in = ParameterIn.QUERY),
            @Parameter(name = "order", description = "Sort order (-name for descending)", in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getAllPaid(@RequestParam(defaultValue = "1", required = false) int page,
                                        @RequestParam(required = false) String order,
                                        @RequestParam(required = false) String My,
                                        @Parameter(hidden = true) @ModelAttribute PaidFilter filter, Authentication authentication, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user) throws IOException {
        int pageSize = 50;
        if (order == null) {
            order = "id";
        }

        Sort sort = order.startsWith("-") ? Sort.by(order.substring(1)).descending() : Sort.by(order).ascending();
        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);

        List<String> filterParams = new ArrayList<>();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Paid> cq = cb.createQuery(Paid.class);
        Root<Paid> root = cq.from(Paid.class);
        List<Predicate> predicates = new ArrayList<>();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String currentUser = ((UserDetailsImpl) principal).getEmail();
            User byEmail = userRepository.findByEmail(currentUser).orElseThrow(SecurityException::new);

            if (My != null) {
                predicates.add(cb.equal(root.get("user").get("email"), currentUser));
                filterParams.add("My=" + My + currentUser);
            }
            if (filter.getId() != null) {
                filterParams.add("id=" + filter.getId());
                if (!paidRepository.existsById(filter.getId())) {
                    return new ResponseEntity<>(new MessageResponse("Entity with id " + filter.getId() + " not found"), HttpStatus.NOT_FOUND);
                }
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getGroup() != null) {
                Group byName = groupRepository.findByName(filter.getGroup());
                filterParams.add("group=" + filter.getGroup());
                predicates.add(cb.equal(root.get("group"), byName));
            }
            if (filter.getCourse() != null) {
                predicates.add(cb.like(root.get("course"), "%" + filter.getCourse() + "%"));
                filterParams.add("course=" + filter.getCourse());
            }
            if (filter.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + filter.getName() + "%"));
                filterParams.add("name=" + filter.getName());
            }
            if (filter.getSurname() != null) {
                predicates.add(cb.like(root.get("surname"), "%" + filter.getSurname() + "%"));
                filterParams.add("surname=" + filter.getSurname());
            }
            if (filter.getEmail() != null) {
                predicates.add(cb.like(root.get("email"), "%" + filter.getEmail() + "%"));
                filterParams.add("email=" + filter.getEmail());
            }
            if (filter.getPhone() != null) {
                predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone() + "%"));
                filterParams.add("phone=" + filter.getPhone());
            }
            if (filter.getAge() != null) {
                predicates.add(cb.equal(root.get("age"), filter.getAge()));
                filterParams.add("age=" + filter.getAge());
            }
            if (filter.getCourseFormat() != null) {
                predicates.add(cb.equal(root.get("courseFormat"), filter.getCourseFormat()));
                filterParams.add("courseFormat=" + filter.getCourseFormat());
            }
            if (filter.getCourseType() != null) {
                predicates.add(cb.equal(root.get("courseType"), filter.getCourseType()));
                filterParams.add("courseType=" + filter.getCourseType());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null;
            Date endDate = null;
            if (filter.getStartDate() != null) {
                try {
                    startDate = dateFormat.parse(filter.getStartDate());
                    filterParams.add("startDate=" + startDate);
                } catch (ParseException e) {
                }
            }
            if (filter.getEndDate() != null) {
                try {
                    endDate = dateFormat.parse(filter.getEndDate());
                    Calendar c = Calendar.getInstance();
                    c.setTime(endDate);
                    c.add(Calendar.DATE, 1);
                    endDate = c.getTime();
                    filterParams.add("endDate=" + endDate);
                } catch (ParseException e) {
                }
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createdAt"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }
            if (filter.getStatus() != null) {
                List<Status> allowedStatuses = Arrays.asList(Status.AGREE, Status.DISAGREE, Status.NEW, Status.INWORK, Status.DUBBING);
                try {
                    Status status = Status.valueOf(filter.getStatus().toUpperCase());
                    if (!allowedStatuses.contains(status)) {
                        return new ResponseEntity<>("Invalid status. Allowed statuses are: " + allowedStatuses, HttpStatus.BAD_REQUEST);
                    }
                    filterParams.add("status=" + status);
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    return new ResponseEntity<>("Invalid status. Allowed statuses are: " + allowedStatuses, HttpStatus.BAD_REQUEST);
                }
            }

            String filters = String.join("&", filterParams);
            cq.where(predicates.toArray(new Predicate[0]));

            if (order.startsWith("-")) {
                cq.orderBy(cb.desc(root.get(order.substring(1))));
            } else {
                cq.orderBy(cb.asc(root.get(order)));
            }

            TypedQuery<Paid> query = em.createQuery(cq);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            List<Paid> resultList = query.getResultList();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            countQuery.select(cb.count(countQuery.from(Paid.class)));
            countQuery.where(predicates.toArray(new Predicate[0]));
            Long count = em.createQuery(countQuery).getSingleResult();

            Page<Paid> results = new PageImpl<>(resultList, pageable, count);
            return new ResponseEntity<>(results, HttpStatus.OK);
        }

        return null;
    }

    @Operation(summary = "get excel file",
            operationId = "getexcel",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Paid.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @PreAuthorize("#user.is_active")
    @SecurityRequirement(name = "JWT")
    @Parameters({
            @Parameter(name = "page", description = "A page number within the paginated result set.", in = ParameterIn.QUERY),
            @Parameter(name = "id", in = ParameterIn.QUERY),
            @Parameter(name = "course", in = ParameterIn.QUERY),
            @Parameter(name = "name", in = ParameterIn.QUERY),
            @Parameter(name = "surname", in = ParameterIn.QUERY),
            @Parameter(name = "email", in = ParameterIn.QUERY),
            @Parameter(name = "phone", in = ParameterIn.QUERY),
            @Parameter(name = "age", in = ParameterIn.QUERY),
            @Parameter(name = "group", in = ParameterIn.QUERY),
            @Parameter(name = "courseFormat", in = ParameterIn.QUERY),
            @Parameter(name = "courseType", in = ParameterIn.QUERY),
            @Parameter(name = "startDate", in = ParameterIn.QUERY),
            @Parameter(name = "endDate", in = ParameterIn.QUERY),
            @Parameter(name = "status", in = ParameterIn.QUERY),
            @Parameter(name = "order", description = "Sort order (-name for descending)", in = ParameterIn.QUERY)
    })

    @GetMapping("/excel")
    public ResponseEntity<?> downloadPaidExcel(
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String My,
            @Parameter(hidden = true) @ModelAttribute PaidFilter filter,
            Authentication authentication,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user) throws IOException {

        if (order == null) {
            order = "id";
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Paid> cq = cb.createQuery(Paid.class);
        Root<Paid> root = cq.from(Paid.class);
        List<Predicate> predicates = new ArrayList<>();
        Long id = filter.getId();

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String currentUser = ((UserDetailsImpl) principal).getEmail();
            User byEmail = userRepository.findByEmail(currentUser).orElseThrow(SecurityException::new);

            if (My != null) {
                predicates.add(cb.equal(root.get("user").get("email"), currentUser));
            }
            if (filter.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filter.getId()));
            }
            if (filter.getGroup() != null) {
                Group byName = groupRepository.findByName(filter.getGroup());
                predicates.add(cb.equal(root.get("group"), byName));
            }
            if (filter.getCourse() != null) {
                predicates.add(cb.like(root.get("course"), "%" + filter.getCourse() + "%"));
            }
            if (filter.getName() != null) {
                predicates.add(cb.like(root.get("name"), "%" + filter.getName() + "%"));
            }
            if (filter.getSurname() != null) {
                predicates.add(cb.like(root.get("surname"), "%" + filter.getSurname() + "%"));
            }
            if (filter.getEmail() != null) {
                predicates.add(cb.like(root.get("email"), "%" + filter.getEmail() + "%"));
            }
            if (filter.getPhone() != null) {
                predicates.add(cb.like(root.get("phone"), "%" + filter.getPhone() + "%"));
            }
            if (filter.getAge() != null) {
                predicates.add(cb.equal(root.get("age"), filter.getAge()));
            }
            if (filter.getCourseFormat() != null) {
                predicates.add(cb.equal(root.get("courseFormat"), filter.getCourseFormat()));
            }
            if (filter.getCourseType() != null) {
                predicates.add(cb.equal(root.get("courseType"), filter.getCourseType()));
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = null;
            Date endDate = null;
            if (filter.getStartDate() != null) {
                try {
                    startDate = dateFormat.parse(filter.getStartDate());
                } catch (ParseException e) {
                }
            }
            if (filter.getEndDate() != null) {
                try {
                    endDate = dateFormat.parse(filter.getEndDate());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(endDate);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    endDate = calendar.getTime();
                } catch (ParseException e) {
                }
            }

            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get("createdAt"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            } else if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }

            if (filter.getStatus() != null) {
                List<Status> allowedStatuses = Arrays.asList(Status.AGREE, Status.DISAGREE, Status.NEW, Status.INWORK, Status.DUBBING);
                if (!allowedStatuses.contains(Status.valueOf(filter.getStatus().toUpperCase()))) {
                    return new ResponseEntity<>("Invalid status. Allowed statuses are: " + allowedStatuses, HttpStatus.BAD_REQUEST);
                }
                predicates.add(cb.equal(root.get("status"), Status.valueOf(filter.getStatus())));
            }


            cq.where(predicates.toArray(new Predicate[0]));

            if (order.startsWith("-")) {
                cq.orderBy(cb.desc(root.get(order.substring(1))));
            } else {
                cq.orderBy(cb.asc(root.get(order)));
            }

            TypedQuery<Paid> query = em.createQuery(cq);

            List<Paid> resultList = query.getResultList();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            countQuery.select(cb.count(countQuery.from(Paid.class)));
            countQuery.where(predicates.toArray(new Predicate[0]));
            Long count = em.createQuery(countQuery).getSingleResult();

            Page<Paid> results = new PageImpl<>(resultList);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Paid");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Surname");
            header.createCell(3).setCellValue("Email");
            header.createCell(4).setCellValue("Phone");
            header.createCell(5).setCellValue("Age");
            header.createCell(6).setCellValue("Course");
            header.createCell(7).setCellValue("Course Format");
            header.createCell(8).setCellValue("Course Type");
            header.createCell(9).setCellValue("Status");
            header.createCell(10).setCellValue("Group");
            header.createCell(11).setCellValue("Sum");
            header.createCell(12).setCellValue("Already Paid");
            header.createCell(13).setCellValue("Manager");
            header.createCell(14).setCellValue("Created At");
            int rowNum = 1;
            for (Paid paid : results.getContent()) {
                Row row = sheet.createRow(rowNum++);
                if (paid.getId() != null) {
                    row.createCell(0).setCellValue(paid.getId());
                } else {
                    row.createCell(0).setCellValue(" ");
                }
                if (paid.getName() != null) {
                    row.createCell(1).setCellValue(paid.getName());
                } else {
                    row.createCell(1).setCellValue(" ");
                }
                if (paid.getSurname() != null) {
                    row.createCell(2).setCellValue(paid.getSurname());
                } else {
                    row.createCell(2).setCellValue(" ");
                }
                if (paid.getEmail() != null) {
                    row.createCell(3).setCellValue(paid.getEmail());
                } else {
                    row.createCell(3).setCellValue(" ");
                }
                if (paid.getPhone() != null) {
                    row.createCell(4).setCellValue(paid.getPhone());
                } else {
                    row.createCell(4).setCellValue(" ");
                }
                if (paid.getAge() != null) {
                    row.createCell(5).setCellValue(paid.getAge());
                } else {
                    row.createCell(5).setCellValue(" ");
                }
                if (paid.getCourse() != null) {
                    row.createCell(6).setCellValue(paid.getCourse());
                } else {
                    row.createCell(6).setCellValue(" ");
                }
                if (paid.getCourseFormat() != null) {
                    row.createCell(7).setCellValue(paid.getCourseFormat());
                } else {
                    row.createCell(7).setCellValue(" ");
                }
                if (paid.getCourseType() != null) {
                    row.createCell(8).setCellValue(paid.getCourseType());
                } else {
                    row.createCell(8).setCellValue(" ");
                }
                if (paid.getStatus() != null) {
                    row.createCell(9).setCellValue(paid.getStatus().toString());
                } else {
                    row.createCell(9).setCellValue(" ");
                }
                if (paid.getGroup() != null) {
                    row.createCell(10).setCellValue(paid.getGroup().getName());
                } else {
                    row.createCell(10).setCellValue(" ");
                }
                if (paid.getSum() != null) {
                    row.createCell(11).setCellValue(paid.getSum());
                } else {
                    row.createCell(11).setCellValue(" ");
                }
                if (paid.getAlreadyPaid() != null) {
                    row.createCell(12).setCellValue(paid.getAlreadyPaid());
                } else {
                    row.createCell(12).setCellValue(" ");
                }
                if (paid.getUser() != null) {
                    row.createCell(13).setCellValue(paid.getUser().getProfile().getName());
                } else {
                    row.createCell(13).setCellValue(" ");
                }
                if (paid.getCreatedAt() != null) {
                    row.createCell(14).setCellValue(paid.getCreatedAt().toString());
                } else {
                    row.createCell(14).setCellValue(" ");
                }


            }

            for (int i = 0; i < header.getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            workbook.write(stream);
            InputStreamResource file = new InputStreamResource(new ByteArrayInputStream(stream.toByteArray()));
            String filename = "Paid.xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .contentLength(stream.size())
                    .body(file);
        }
        return null;
    }


    @Operation(summary = "getpaidbyid",
            operationId = "getAllPaid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Paid.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @GetMapping("/{id}")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<?> getPaidById(@PathVariable long id) {
        Optional<Paid> one = paidRepository.findById(id);
        if (!one.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(one, HttpStatus.OK);
    }

    @Operation(summary = "patch paid by id",
            operationId = "patchbyid",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Paid.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request")
            })
    @PatchMapping("/{id}")
    @PreAuthorize("#user.is_active")
    @SecurityRequirement(name = "JWT")
    public ResponseEntity<?> setPaidParam(
            @PathVariable long id, Authentication authentication, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user, @RequestBody(required = false) PaidFilter filter) {

        Optional<Paid> one = paidRepository.findById(id);
        if (!one.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        Paid paid = paidRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("paid not found"));
        Object principal = authentication.getPrincipal();


        if (principal instanceof UserDetails) {
            String currentUser = ((UserDetailsImpl) principal).getEmail();
            User byEmail = userRepository.findByEmail(currentUser).orElseThrow(SecurityException::new);


            paid.setStatus(Status.INWORK);
            paid.setUser(byEmail);


            if (paid == null) {
                return ResponseEntity.badRequest().build();
            }
            if (filter != null && filter.getCourse() != null) {
                paid.setCourse(filter.getCourse());
                checkUserAndSave(paid, byEmail);
            }
            if (filter != null && filter.getGroup() != null) {
                Group byName = groupRepository.findByName(filter.getGroup());
                if (byName != null) {
                    paid.setGroup(byName);
                    checkUserAndSave(paid, byEmail);
                } else {
                    List<Group> allGroups = groupRepository.findAll();
                    String errorMessage = "Group not found. Available groups: " +
                            allGroups.stream().map(Group::getName).collect(Collectors.joining(", "));
                    return ResponseEntity.badRequest().body(errorMessage);
                }
            }
            if (filter != null) {
                String name = filter.getName();
                if (StringUtils.isBlank(name) || name.matches("^[a-zA-Zа-яА-ЯёЁґҐєЄіїІЇ -]{1,20}$")) {
                    paid.setName(name);
                    checkUserAndSave(paid, byEmail);
                } else {
                    return ResponseEntity.badRequest().body("Invalid name format. Please enter a name with only letters and dashes.");
                }
            }
            if (filter != null && filter.getSurname() != null) {
                String surname = filter.getSurname();
                if (StringUtils.isBlank(surname) || surname.matches("^[a-zA-Zа-яА-ЯёЁґҐєЄіїІЇ -]{1,20}$")) {
                    paid.setSurname(surname);
                    checkUserAndSave(paid, byEmail);
                } else {
                    return ResponseEntity.badRequest().body("Invalid surname format. Please enter a surname with only letters and dashes.");
                }
            }

            if (filter != null && filter.getEmail() != null) {
                String email = filter.getEmail();
                if (StringUtils.isBlank(email) || email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                    paid.setEmail(email);
                    checkUserAndSave(paid, byEmail);
                } else {
                    return ResponseEntity.badRequest().body("Invalid email format. Please enter a valid email address.");
                }
            }
            if (filter != null && filter.getPhone() != null) {
                String phoneNumber = filter.getPhone();
                if (StringUtils.isBlank(phoneNumber) || isValidUkrainianPhoneNumber(phoneNumber)) {
                    paid.setPhone(phoneNumber);
                    checkUserAndSave(paid, byEmail);
                } else {
                    String examplePhoneNumber = "380734378687";
                    return ResponseEntity.badRequest().body("Invalid phone number format. Please enter a valid Ukrainian phone number, for example: " + examplePhoneNumber);
                }
            }

            if (filter.getGroup() != null) {
                Group byName = groupRepository.findByName(filter.getGroup());
                paid.setGroup(byName);
                checkUserAndSave(paid, byEmail);
            }
            if (filter != null && filter.getAge() != null) {
                Integer age = filter.getAge();
                if (age < 16 || age > 100) {
                    return ResponseEntity.badRequest().body("Возраст должен быть между 16 и 100 годами.");
                }
                paid.setAge(age);
                checkUserAndSave(paid, byEmail);
            }

            if (filter != null && filter.getCourseFormat() != null) {
                paid.setCourseFormat(filter.getCourseFormat());
                checkUserAndSave(paid, byEmail);
            }
            if (filter != null && filter.getCourseType() != null) {
                paid.setCourseType(filter.getCourseType());
                checkUserAndSave(paid, byEmail);
            }
            if (filter != null && filter.getSum() != null) {
                paid.setSum(filter.getSum());
                checkUserAndSave(paid, byEmail);
            }
            if (filter != null && filter.getStatus() != null) {
                if (filter.getStatus().toUpperCase().equals(Status.AGREE.toString())) {
                    paid.setStatus(Status.AGREE);
                } else if (filter.getStatus().toUpperCase().equals(Status.INWORK.toString())) {
                    paid.setStatus(Status.INWORK);
                } else if (filter.getStatus().toUpperCase().equals(Status.DISAGREE.toString())) {
                    paid.setStatus(Status.DISAGREE);
                } else if (filter.getStatus().toUpperCase().equals(Status.DUBBING.toString())) {
                    paid.setStatus(Status.DUBBING);
                } else if (filter.getStatus().toUpperCase().equals(Status.NEW.toString())) {
                    paid.setStatus(Status.NEW);
                    paid.setUser(null);
                    checkUserAndSave(paid, byEmail);
                }
                checkUserAndSave(paid, byEmail);
            }
            if (filter != null && filter.getAlreadyPaid() != null) {
                int alreadyPaid = filter.getAlreadyPaid();
                String alreadyPaidStr = Integer.toString(alreadyPaid);
                for (int i = 0; i < alreadyPaidStr.length(); i++) {
                    if (!Character.isDigit(alreadyPaidStr.charAt(i))) {
                        return ResponseEntity.badRequest().body("Already paid value must be a number");
                    }
                }
                paid.setAlreadyPaid(alreadyPaid);
                checkUserAndSave(paid, byEmail);
            }


            if (filter != null && filter.getComment() != null) {
                LocalDate date = LocalDate.now();
                Comment comment1 = new Comment();
                comment1.setComment(filter.getComment());
                comment1.setCreated_at(date);
                comment1.setPaid(paid);
                comment1.setUser(byEmail);
                commentRepository.save(comment1);
                checkUserAndSave(paid, byEmail);

            }
            if (paid.getStatus() == Status.NEW) {
                paid.setUser(null);
            }
            paidRepository.save(paid);
            return new ResponseEntity(paid, HttpStatus.OK);
        }
        return null;
    }


    @Operation(summary = "Create a new group",
            operationId = "CreateGroup",
            parameters = {
                    @Parameter(name = "name", description = "The name of the group", required = true, example = "My Group")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Group created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request - group name is missing or empty"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PreAuthorize("#user.is_active")
    @PostMapping("/group")
    public ResponseEntity<?> createGroup(@RequestBody Group group, @Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user) {
        String name = group.getName();

        if (name == null || name.isEmpty() || name.length() > 10 || name.contains(" ") || !name.matches(".*[a-zA-Z].*") || groupRepository.existsByName(name)) {
            if (name == null || name.isEmpty()) {
                return ResponseEntity.badRequest().body("Group name is required");
            }
            if (name.length() > 10) {
                return ResponseEntity.badRequest().body("Group name must be 10 characters or less");
            }
            if (name.contains(" ")) {
                return ResponseEntity.badRequest().body("Group name should not contain spaces");
            }
            if (!name.matches(".*[a-zA-Z0-9_].*") || name.matches("[^a-zA-Z0-9_]+")) {
                return ResponseEntity.badRequest().body("Group name is invalid");
            }
            if (!name.matches(".*[a-zA-Z].*")) {
                return ResponseEntity.badRequest().body("Group name must contain at least one letter");
            }

            if (groupRepository.existsByName(name)) {
                return ResponseEntity.badRequest().body("Group name is used");
            }
        }

        Group newGroup = new Group();
        newGroup.setName(name);
        Group savedGroup = groupRepository.save(newGroup);

        return ResponseEntity.ok(savedGroup);
    }


    @Operation(summary = "Get all groups",
            operationId = "GetAllGroups",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all groups returned successfully", content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Group.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PreAuthorize("#user.is_active")
    @GetMapping("/group")
    public ResponseEntity<?> getAllGroup(@Parameter(hidden = true) @AuthenticationPrincipal UserDetailsImpl user) {
        List<Group> all = groupRepository.findAll();
        return new ResponseEntity<>(all, HttpStatus.OK);
    }


    private void checkUserAndSave(Paid paid, User byEmail) {
        if (byEmail != paid.getUser() && paid.getUser() != null) {
            throw new IllegalArgumentException("you can't do that");
        } else if (paid.getUser() == null) {
            paid.setUser(byEmail);
            paidRepository.save(paid);
        }
    }

    private boolean isValidUkrainianPhoneNumber(String phoneNumber) {
        // Проверяем, соответствует ли номер телефона формату 380XXXXXXXXX
        return phoneNumber.matches("^380\\d{9}$");
    }


}
