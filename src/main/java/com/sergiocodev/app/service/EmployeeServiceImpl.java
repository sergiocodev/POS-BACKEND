package com.sergiocodev.app.service;

import com.sergiocodev.app.dto.employee.EmployeeRequest;
import com.sergiocodev.app.dto.employee.EmployeeResponse;
import com.sergiocodev.app.model.Employee;
import com.sergiocodev.app.repository.EmployeeRepository;
import com.sergiocodev.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Employee entity = new Employee();
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setDocumentNumber(request.documentNumber());
        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
        }
        entity.setActive(request.active());
        return new EmployeeResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return repository.findAll().stream()
                .map(EmployeeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return repository.findById(id)
                .map(EmployeeResponse::new)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setDocumentNumber(request.documentNumber());
        if (request.userId() != null) {
            entity.setUser(userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
        } else {
            entity.setUser(null);
        }
        entity.setActive(request.active());
        return new EmployeeResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
