package com.fpt.backend;

import com.fpt.backend.service.impl.contract.ContractWorkflowSchemaMigration;
import com.fpt.backend.service.impl.signature.SignatureSchemaMigration;
import com.fpt.backend.mail.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class BackendApplicationTests {

	@MockitoBean
	private ContractWorkflowSchemaMigration contractWorkflowSchemaMigration;

	@MockitoBean
	private SignatureSchemaMigration signatureSchemaMigration;

	@MockitoBean
	private EmailService emailService;

	@Test
	void contextLoads() {
	}

}
