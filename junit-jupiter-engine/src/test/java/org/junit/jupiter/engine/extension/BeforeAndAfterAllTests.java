/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.ClassSelector.selectClass;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link BeforeAll}, {@link AfterAll},
 * {@link BeforeAllCallback}, and {@link AfterAllCallback} in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public class BeforeAndAfterAllTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@Test
	void beforeAllAndAfterAllCallbacks() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(TopLevelTestCase.class,
			"fooBeforeAll",
			"barBeforeAll",
				"beforeAllMethod-1",
					"test-1",
				"afterAllMethod-1",
			"barAfterAll",
			"fooAfterAll"
		);
		// @formatter:on
	}

	@Test
	void beforeAllAndAfterAllCallbacksInSubclass() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(SecondLevelTestCase.class,
			"fooBeforeAll",
			"barBeforeAll",
				"bazBeforeAll",
					"beforeAllMethod-1",
						"beforeAllMethod-2",
							"test-2",
						"afterAllMethod-2",
					"afterAllMethod-1",
				"bazAfterAll",
			"barAfterAll",
			"fooAfterAll"
		);
		// @formatter:on
	}

	@Test
	void beforeAllAndAfterAllCallbacksInSubSubclass() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(ThirdLevelTestCase.class,
			"fooBeforeAll",
			"barBeforeAll",
				"bazBeforeAll",
					"quuxBeforeAll",
						"beforeAllMethod-1",
							"beforeAllMethod-2",
								"beforeAllMethod-3",
									"test-3",
								"afterAllMethod-3",
							"afterAllMethod-2",
						"afterAllMethod-1",
					"quuxAfterAll",
				"bazAfterAll",
			"barAfterAll",
			"fooAfterAll"
		);
		// @formatter:on
	}

	private void assertBeforeAllAndAfterAllCallbacks(Class<?> testClass, String... expectedCalls) {
		callSequence.clear();
		TestDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		assertEquals(asList(expectedCalls), callSequence, () -> "wrong call sequence for " + testClass.getName());
	}

	// -------------------------------------------------------------------------

	// Must NOT be private; otherwise, the @Test method gets discovered but never executed.
	@ExtendWith({ FooClassLevelCallbacks.class, BarClassLevelCallbacks.class })
	static class TopLevelTestCase {

		@BeforeAll
		static void beforeAll1() {
			callSequence.add("beforeAllMethod-1");
		}

		@AfterAll
		static void afterAll1() {
			callSequence.add("afterAllMethod-1");
		}

		@Test
		void test() {
			callSequence.add("test-1");
		}
	}

	// Must NOT be private; otherwise, the @Test method gets discovered but never executed.
	@ExtendWith(BazClassLevelCallbacks.class)
	static class SecondLevelTestCase extends TopLevelTestCase {

		@BeforeAll
		static void beforeAll2() {
			callSequence.add("beforeAllMethod-2");
		}

		@AfterAll
		static void afterAll2() {
			callSequence.add("afterAllMethod-2");
		}

		@Test
		@Override
		void test() {
			callSequence.add("test-2");
		}
	}

	@ExtendWith(QuuxClassLevelCallbacks.class)
	private static class ThirdLevelTestCase extends SecondLevelTestCase {

		@BeforeAll
		static void beforeAll3() {
			callSequence.add("beforeAllMethod-3");
		}

		@AfterAll
		static void afterAll3() {
			callSequence.add("afterAllMethod-3");
		}

		@Test
		@Override
		void test() {
			callSequence.add("test-3");
		}
	}

	private static class FooClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooBeforeAll");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooAfterAll");
		}
	}

	private static class BarClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barBeforeAll");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barAfterAll");
		}
	}

	private static class BazClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("bazBeforeAll");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("bazAfterAll");
		}
	}

	private static class QuuxClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("quuxBeforeAll");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("quuxAfterAll");
		}
	}

}
