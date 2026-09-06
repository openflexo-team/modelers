package org.openflexo.modelers.statecharts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Loads and validates the three {@link VirtualModel} of the StateCharts modeler.
 *
 * <p>
 * This test deliberately asserts the <em>expected content</em> of each virtual model (the exact set of {@link FlexoConcept} names, and the
 * number of declared behaviours) rather than only calling <code>assertVirtualModelIsValid()</code>. A failed FML parse leaves an
 * <em>empty</em> compilation unit behind, and an empty compilation unit validates with zero error: a validation-only test would be green
 * while the modeler is in fact not loaded at all.
 *
 * <p>
 * What this test does NOT do is run the modeler: that is the job of the FML-scripts of <code>src/main/resources/AutomatedTests</code>, see
 * {@link StateChartsModelerAutomatedTests}.
 *
 * <p>
 * Note that StateChartDiagram only validates against the FML attributes added to the diagram technology adapter, so this suite must be run
 * through that included build (or against a published snapshot that carries them):
 *
 * <pre>
 * ./gradlew --include-build ../openflexo-diagram :statecharts-modeler:test
 * </pre>
 */
@RunWith(OrderedRunner.class)
public class TestStateChartsModeler extends OpenflexoTestCase {

	protected static final Logger logger = Logger.getLogger(TestStateChartsModeler.class.getPackage().getName());

	/**
	 * Describes what a given {@link VirtualModel} is expected to contain once loaded.
	 */
	private static class Expectation {

		private final String name;
		private final int declaredBehaviours;
		private final List<String> conceptNames;

		Expectation(String name, int declaredBehaviours, String... conceptNames) {
			this.name = name;
			this.declaredBehaviours = declaredBehaviours;
			this.conceptNames = Arrays.asList(conceptNames);
		}
	}

	/**
	 * Instantiate test service manager, activating the only technology adapter this modeler mounts
	 */
	@Test
	@TestOrder(1)
	public void test0InstantiateResourceCenter() {

		log("test0InstantiateResourceCenter()");

		instanciateTestServiceManager(DiagramTechnologyAdapter.class);
	}

	@Test
	@TestOrder(2)
	public void test1StateCharts() {

		log("test1StateCharts()");

		testModeler(StateChartsModelerConstants.STATECHARTS_URI, new Expectation("StateCharts", 3),
				new Expectation("StateChartModel", 2, "InitialTransition", "State", "Transition"),
				new Expectation("StateChartDiagram", 1, "InitialTransitionGR", "StateGR", "TransitionGR"));
	}

	/**
	 * Load the modeler identified by supplied URI, together with all the {@link VirtualModel} it contains, and assert that each of them
	 * holds the content described by supplied expectations, then that each of them is valid.
	 */
	private void testModeler(String modelerURI, Expectation rootExpectation, Expectation... containedExpectations) {

		log("Testing modeler loading: " + modelerURI);

		CompilationUnitResource rootResource = (CompilationUnitResource) serviceManager.getResourceManager().getResource(modelerURI);
		assertNotNull("No resource found for URI " + modelerURI, rootResource);

		VirtualModel rootVirtualModel = rootResource.getCompilationUnit().getVirtualModel();
		assertNotNull("No VirtualModel in " + modelerURI, rootVirtualModel);
		assertTrue(rootResource.isLoaded());
		System.out.println("Loaded " + rootVirtualModel.getName() + " from " + rootResource.getIODelegate().toString());

		List<VirtualModel> allVirtualModels = new ArrayList<>();
		allVirtualModels.add(rootVirtualModel);

		for (FlexoResource<?> containedResource : rootResource.getContents()) {
			if (containedResource instanceof CompilationUnitResource) {
				VirtualModel containedVirtualModel = ((CompilationUnitResource) containedResource).getCompilationUnit().getVirtualModel();
				assertNotNull("No VirtualModel in " + containedResource.getURI(), containedVirtualModel);
				allVirtualModels.add(containedVirtualModel);
			}
		}

		List<Expectation> expectations = new ArrayList<>();
		expectations.add(rootExpectation);
		expectations.addAll(Arrays.asList(containedExpectations));

		assertEquals("Unexpected set of VirtualModels in " + modelerURI, names(expectations), loadedNames(allVirtualModels));

		for (Expectation expectation : expectations) {
			assertVirtualModelMatches(expectation, virtualModelNamed(expectation.name, allVirtualModels));
		}

		for (VirtualModel virtualModel : allVirtualModels) {
			assertVirtualModelIsValid(virtualModel);
		}
	}

	private void assertVirtualModelMatches(Expectation expectation, VirtualModel virtualModel) {

		System.out.println("Checking VirtualModel " + virtualModel.getName());
		System.out.println(virtualModel.getFMLPrettyPrint());

		// A failed parse leaves an EMPTY compilation unit behind, and an empty unit validates with
		// zero error: check the actual content before trusting assertVirtualModelIsValid() below.
		assertEquals("Unexpected FlexoConcepts in VirtualModel " + expectation.name, new TreeSet<>(expectation.conceptNames),
				conceptNames(virtualModel));

		assertEquals("Unexpected number of declared behaviours in VirtualModel " + expectation.name, expectation.declaredBehaviours,
				virtualModel.getDeclaredFlexoBehaviours().size());
	}

	private static TreeSet<String> conceptNames(VirtualModel virtualModel) {
		TreeSet<String> returned = new TreeSet<>();
		for (FlexoConcept concept : virtualModel.getFlexoConcepts()) {
			returned.add(concept.getName());
		}
		return returned;
	}

	private static TreeSet<String> names(List<Expectation> expectations) {
		TreeSet<String> returned = new TreeSet<>();
		for (Expectation expectation : expectations) {
			returned.add(expectation.name);
		}
		return returned;
	}

	private static TreeSet<String> loadedNames(List<VirtualModel> virtualModels) {
		TreeSet<String> returned = new TreeSet<>();
		for (VirtualModel virtualModel : virtualModels) {
			returned.add(virtualModel.getName());
		}
		return returned;
	}

	private static VirtualModel virtualModelNamed(String name, List<VirtualModel> virtualModels) {
		for (VirtualModel virtualModel : virtualModels) {
			if (name.equals(virtualModel.getName())) {
				return virtualModel;
			}
		}
		throw new AssertionError("No VirtualModel named " + name);
	}
}
