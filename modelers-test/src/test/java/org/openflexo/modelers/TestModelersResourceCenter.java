package org.openflexo.modelers;

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
import org.openflexo.technologyadapter.emf.EMFTechnologyAdapter;
import org.openflexo.technologyadapter.gina.GINATechnologyAdapter;
import org.openflexo.technologyadapter.owl.OWLTechnologyAdapter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Loads and validates every {@link VirtualModel} shipped by the <code>modelers-rc</code> resource center.
 *
 * <p>
 * This test deliberately asserts the <em>expected content</em> of each virtual model (the exact set of {@link FlexoConcept} names, and the
 * number of declared behaviours) rather than only calling <code>assertVirtualModelIsValid()</code>. A failed FML parse leaves an
 * <em>empty</em> compilation unit behind, and an empty compilation unit validates with zero error: a validation-only test would be green
 * while the modeler is in fact not loaded at all.
 *
 * <p>
 * The expectations below were extracted from the serialized models themselves, so they describe what each modeler is supposed to contain,
 * independently of the serialization format it is currently stored in.
 *
 * <p>
 * As of this writing all four modelers fail here: none of the shipped <code>.fml</code> files parses against the 2.99 grammar. The tests are
 * expected to turn green one by one as each modeler is migrated.
 */
@RunWith(OrderedRunner.class)
public class TestModelersResourceCenter extends OpenflexoTestCase {

	protected static final Logger logger = Logger.getLogger(TestModelersResourceCenter.class.getPackage().getName());

	private static final String STATECHARTS_EDITOR_URI = "http://openflexo.org/modellers/StateCharts.fml";
	private static final String UML_EDITOR_URI = "http://openflexo.org/modellers/resources/UMLEditor/UMLEditor.viewpoint";
	private static final String OWL_ONTOLOGY_EDITOR_URI = "http://openflexo.org/modellers/OWL/FML/OWLOntologyEditor.fml";

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
	 * Instantiate test service manager, activating the technology adapters required by the modelers
	 */
	@Test
	@TestOrder(1)
	public void test0InstantiateResourceCenter() {

		log("test0InstantiateResourceCenter()");

		instanciateTestServiceManager(EMFTechnologyAdapter.class, DiagramTechnologyAdapter.class, GINATechnologyAdapter.class,
				OWLTechnologyAdapter.class);
	}

	@Test
	@TestOrder(2)
	public void test1BPMNEditor() {

		log("test1BPMNEditor()");

		testModeler(ModelersConstants.BPMN_EDITOR_URI, new Expectation("BPMNEditor", 5),
				new Expectation("BPMNModel", 4, "BaseElement", "CatchEvent", "EndEvent", "Event", "ExclusiveGateway", "FlowElement",
						"FlowNode", "Gateway", "ParallelGateway", "Process", "SequenceFlow", "StartEvent", "Task", "ThrowEvent"),
				new Expectation("ProcessDiagram", 4, "EndEventGR", "ExclusiveGatewayGR", "FlowNodeGR", "GatewayGR", "ParallelGatewayGR",
						"SequenceFlowGR", "StartEventGR", "TaskGR"));
	}

	@Test
	@TestOrder(3)
	public void test2StateCharts() {

		log("test2StateCharts()");

		testModeler(STATECHARTS_EDITOR_URI, new Expectation("StateCharts", 3),
				new Expectation("StateChartModel", 2, "InitialTransition", "State", "Transition"),
				new Expectation("StateChartDiagram", 1, "InitialTransitionGR", "StateGR", "TransitionGR"));
	}

	@Test
	@TestOrder(4)
	public void test3UMLEditor() {

		log("test3UMLEditor()");

		testModeler(UML_EDITOR_URI, new Expectation("UMLEditor", 1), new Expectation("UMLModel", 1, "Class", "Property"),
				new Expectation("UMLClassDiagram", 1, "ClassGR"));
	}

	@Test
	@TestOrder(5)
	public void test4OWLOntologyEditor() {

		log("test4OWLOntologyEditor()");

		// This one is the only modeler already stored as textual FML rather than as legacy .fml.xml,
		// but it was written against the 2023 grammar: its "import [<uri>] as <name>;" declarations
		// are no longer accepted, the current form being "import <Type> <name> from [<uri>];".
		testModeler(OWL_ONTOLOGY_EDITOR_URI,
				new Expectation("OWLOntologyEditor", 8, "IsAGR", "ObjectPropertyGR", "OWLClassGR", "OWLIndividualGR", "SubClassGR"));
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
