package com.nexstar.portal.organization.service;

import com.nexstar.portal.common.exception.BusinessException;
import com.nexstar.portal.common.exception.ResourceNotFoundException;
import com.nexstar.portal.organization.dto.*;
import com.nexstar.portal.organization.entity.*;
import com.nexstar.portal.organization.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final DesignationRepository designationRepository;
    private final BranchRepository branchRepository;
    private final HolidayRepository holidayRepository;

    // ── Company ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CompanyDto getCompany() {
        Company company = companyRepository.findFirstByDeletedFalse()
                .orElseThrow(() -> new ResourceNotFoundException("Company profile not configured"));
        return toCompanyDto(company);
    }

    @Transactional
    public CompanyDto upsertCompany(CompanyDto dto) {
        Company company = companyRepository.findFirstByDeletedFalse()
                .orElse(new Company());
        mapToCompany(dto, company);
        return toCompanyDto(companyRepository.save(company));
    }

    // ── Departments ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartments(UUID companyId) {
        return departmentRepository.findByCompanyIdAndDeletedFalse(companyId)
                .stream().map(this::toDepartmentDto).toList();
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByNameAndCompanyIdAndDeletedFalse(dto.getName(), dto.getCompanyId())) {
            throw new BusinessException("Department with this name already exists");
        }
        Company company = companyRepository.findByIdAndDeletedFalse(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", dto.getCompanyId()));

        Department dept = Department.builder()
                .company(company)
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .active(dto.isActive())
                .build();

        if (dto.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findByIdAndDeletedFalse(dto.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Department", "id", dto.getParentDepartmentId()));
            dept.setParentDepartment(parent);
        }
        return toDepartmentDto(departmentRepository.save(dept));
    }

    @Transactional
    public DepartmentDto updateDepartment(UUID id, DepartmentDto dto) {
        Department dept = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setName(dto.getName());
        dept.setCode(dto.getCode());
        dept.setDescription(dto.getDescription());
        dept.setActive(dto.isActive());
        if (dto.getParentDepartmentId() != null) {
            Department parent = departmentRepository.findByIdAndDeletedFalse(dto.getParentDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Department", "id", dto.getParentDepartmentId()));
            dept.setParentDepartment(parent);
        }
        return toDepartmentDto(departmentRepository.save(dept));
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        Department dept = departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setDeleted(true);
        departmentRepository.save(dept);
    }

    // ── Teams ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeamDto> getTeamsByDepartment(UUID departmentId) {
        return teamRepository.findByDepartmentIdAndDeletedFalse(departmentId)
                .stream().map(this::toTeamDto).toList();
    }

    @Transactional
    public TeamDto createTeam(TeamDto dto) {
        if (teamRepository.existsByNameAndDepartmentIdAndDeletedFalse(dto.getName(), dto.getDepartmentId())) {
            throw new BusinessException("Team with this name already exists in the department");
        }
        Department department = departmentRepository.findByIdAndDeletedFalse(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", dto.getDepartmentId()));

        Team team = Team.builder()
                .department(department)
                .name(dto.getName())
                .code(dto.getCode())
                .description(dto.getDescription())
                .active(dto.isActive())
                .build();
        return toTeamDto(teamRepository.save(team));
    }

    @Transactional
    public TeamDto updateTeam(UUID id, TeamDto dto) {
        Team team = teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        team.setName(dto.getName());
        team.setCode(dto.getCode());
        team.setDescription(dto.getDescription());
        team.setActive(dto.isActive());
        return toTeamDto(teamRepository.save(team));
    }

    @Transactional
    public void deleteTeam(UUID id) {
        Team team = teamRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", id));
        team.setDeleted(true);
        teamRepository.save(team);
    }

    // ── Designations ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DesignationDto> getDesignations(UUID companyId) {
        return designationRepository.findByCompanyIdAndDeletedFalse(companyId)
                .stream().map(this::toDesignationDto).toList();
    }

    @Transactional
    public DesignationDto createDesignation(DesignationDto dto) {
        if (designationRepository.existsByNameAndCompanyIdAndDeletedFalse(dto.getName(), dto.getCompanyId())) {
            throw new BusinessException("Designation with this name already exists");
        }
        Company company = companyRepository.findByIdAndDeletedFalse(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", dto.getCompanyId()));

        Designation designation = Designation.builder()
                .company(company)
                .name(dto.getName())
                .level(dto.getLevel())
                .description(dto.getDescription())
                .active(dto.isActive())
                .build();
        return toDesignationDto(designationRepository.save(designation));
    }

    @Transactional
    public DesignationDto updateDesignation(UUID id, DesignationDto dto) {
        Designation designation = designationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        designation.setName(dto.getName());
        designation.setLevel(dto.getLevel());
        designation.setDescription(dto.getDescription());
        designation.setActive(dto.isActive());
        return toDesignationDto(designationRepository.save(designation));
    }

    @Transactional
    public void deleteDesignation(UUID id) {
        Designation designation = designationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
        designation.setDeleted(true);
        designationRepository.save(designation);
    }

    // ── Branches ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BranchDto> getBranches(UUID companyId) {
        return branchRepository.findByCompanyIdAndDeletedFalse(companyId)
                .stream().map(this::toBranchDto).toList();
    }

    @Transactional
    public BranchDto createBranch(BranchDto dto) {
        Company company = companyRepository.findByIdAndDeletedFalse(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", dto.getCompanyId()));

        Branch branch = Branch.builder()
                .company(company)
                .name(dto.getName())
                .code(dto.getCode())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .timezone(dto.getTimezone())
                .phone(dto.getPhone())
                .headquarters(dto.isHeadquarters())
                .active(dto.isActive())
                .build();
        return toBranchDto(branchRepository.save(branch));
    }

    @Transactional
    public BranchDto updateBranch(UUID id, BranchDto dto) {
        Branch branch = branchRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branch.setName(dto.getName());
        branch.setCode(dto.getCode());
        branch.setAddress(dto.getAddress());
        branch.setCity(dto.getCity());
        branch.setState(dto.getState());
        branch.setCountry(dto.getCountry());
        branch.setTimezone(dto.getTimezone());
        branch.setPhone(dto.getPhone());
        branch.setHeadquarters(dto.isHeadquarters());
        branch.setActive(dto.isActive());
        return toBranchDto(branchRepository.save(branch));
    }

    @Transactional
    public void deleteBranch(UUID id) {
        Branch branch = branchRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branch.setDeleted(true);
        branchRepository.save(branch);
    }

    // ── Holidays ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HolidayDto> getHolidays(UUID companyId) {
        return holidayRepository.findByCompanyIdAndDeletedFalse(companyId)
                .stream().map(this::toHolidayDto).toList();
    }

    @Transactional
    public HolidayDto createHoliday(HolidayDto dto) {
        Company company = companyRepository.findByIdAndDeletedFalse(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", dto.getCompanyId()));

        Holiday holiday = Holiday.builder()
                .company(company)
                .name(dto.getName())
                .date(dto.getDate())
                .type(dto.getType())
                .description(dto.getDescription())
                .optional(dto.isOptional())
                .build();
        return toHolidayDto(holidayRepository.save(holiday));
    }

    @Transactional
    public HolidayDto updateHoliday(UUID id, HolidayDto dto) {
        Holiday holiday = holidayRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));
        holiday.setName(dto.getName());
        holiday.setDate(dto.getDate());
        holiday.setType(dto.getType());
        holiday.setDescription(dto.getDescription());
        holiday.setOptional(dto.isOptional());
        return toHolidayDto(holidayRepository.save(holiday));
    }

    @Transactional
    public void deleteHoliday(UUID id) {
        Holiday holiday = holidayRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));
        holiday.setDeleted(true);
        holidayRepository.save(holiday);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private CompanyDto toCompanyDto(Company c) {
        CompanyDto dto = new CompanyDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setLogoUrl(c.getLogoUrl());
        dto.setIndustry(c.getIndustry());
        dto.setWebsite(c.getWebsite());
        dto.setPhone(c.getPhone());
        dto.setEmail(c.getEmail());
        dto.setAddress(c.getAddress());
        dto.setCity(c.getCity());
        dto.setState(c.getState());
        dto.setCountry(c.getCountry());
        dto.setPostalCode(c.getPostalCode());
        dto.setTimezone(c.getTimezone());
        dto.setCurrency(c.getCurrency());
        dto.setDateFormat(c.getDateFormat());
        dto.setDescription(c.getDescription());
        return dto;
    }

    private void mapToCompany(CompanyDto dto, Company c) {
        c.setName(dto.getName());
        c.setLogoUrl(dto.getLogoUrl());
        c.setIndustry(dto.getIndustry());
        c.setWebsite(dto.getWebsite());
        c.setPhone(dto.getPhone());
        c.setEmail(dto.getEmail());
        c.setAddress(dto.getAddress());
        c.setCity(dto.getCity());
        c.setState(dto.getState());
        c.setCountry(dto.getCountry());
        c.setPostalCode(dto.getPostalCode());
        c.setTimezone(dto.getTimezone());
        c.setCurrency(dto.getCurrency());
        c.setDateFormat(dto.getDateFormat());
        c.setDescription(dto.getDescription());
    }

    private DepartmentDto toDepartmentDto(Department d) {
        DepartmentDto dto = new DepartmentDto();
        dto.setId(d.getId());
        dto.setCompanyId(d.getCompany().getId());
        dto.setName(d.getName());
        dto.setCode(d.getCode());
        dto.setDescription(d.getDescription());
        dto.setActive(d.isActive());
        if (d.getParentDepartment() != null) {
            dto.setParentDepartmentId(d.getParentDepartment().getId());
            dto.setParentDepartmentName(d.getParentDepartment().getName());
        }
        return dto;
    }

    private TeamDto toTeamDto(Team t) {
        TeamDto dto = new TeamDto();
        dto.setId(t.getId());
        dto.setDepartmentId(t.getDepartment().getId());
        dto.setDepartmentName(t.getDepartment().getName());
        dto.setName(t.getName());
        dto.setCode(t.getCode());
        dto.setDescription(t.getDescription());
        dto.setActive(t.isActive());
        return dto;
    }

    private DesignationDto toDesignationDto(Designation d) {
        DesignationDto dto = new DesignationDto();
        dto.setId(d.getId());
        dto.setCompanyId(d.getCompany().getId());
        dto.setName(d.getName());
        dto.setLevel(d.getLevel());
        dto.setDescription(d.getDescription());
        dto.setActive(d.isActive());
        return dto;
    }

    private BranchDto toBranchDto(Branch b) {
        BranchDto dto = new BranchDto();
        dto.setId(b.getId());
        dto.setCompanyId(b.getCompany().getId());
        dto.setName(b.getName());
        dto.setCode(b.getCode());
        dto.setAddress(b.getAddress());
        dto.setCity(b.getCity());
        dto.setState(b.getState());
        dto.setCountry(b.getCountry());
        dto.setTimezone(b.getTimezone());
        dto.setPhone(b.getPhone());
        dto.setHeadquarters(b.isHeadquarters());
        dto.setActive(b.isActive());
        return dto;
    }

    private HolidayDto toHolidayDto(Holiday h) {
        HolidayDto dto = new HolidayDto();
        dto.setId(h.getId());
        dto.setCompanyId(h.getCompany().getId());
        dto.setName(h.getName());
        dto.setDate(h.getDate());
        dto.setType(h.getType());
        dto.setDescription(h.getDescription());
        dto.setOptional(h.isOptional());
        return dto;
    }
}
