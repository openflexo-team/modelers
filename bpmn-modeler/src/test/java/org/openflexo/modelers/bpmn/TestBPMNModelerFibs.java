package org.openflexo.modelers.bpmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.emf.EMFTechnologyAdapter;
import org.openflexo.technologyadapter.gina.FIBComponentModelSlot;
import org.openflexo.technologyadapter.gina.GINATechnologyAdapter;
import org.openflexo.technologyadapter.gina.fml.FMLControlledFIBVirtualModelNature;
import org.openflexo.technologyadapter.gina.model.GINAFIBComponent;
import org.openflexo.technologyadapter.gina.rm.GINAFIBComponentResource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Checks the <code>.fib</code> user interfaces federated by the VirtualModels of this resource center.
 *
 * <p>
 * A <code>.fib</code> is reached from FML through a {@link FIBComponentModelSlot}, whose <code>templateComponentURI</code> is resolved
 * against the resource manager. That link is <em>silent</em> when it breaks:
 * {@link FMLControlledFIBVirtualModelNature#hasNature(VirtualModel)} simply returns false when the URI resolves to nothing, the VirtualModel
 * quietly loses its FML-controlled-FIB nature, and every other test of this project stays green - the FML itself validates, since the URI is
 * only a string attribute. This suite is what makes such a break visible, in particular after the URI rewriting that came with the
 * extraction of this modeler into its own resource center.
 *
 * <p>
 * It is written over the resource center rather than over a hard-coded list of files, so a <code>.fib</code> added later is covered without
 * touching this class, and it walks the link in both directions: every declared model slot must reach a component, and every component
 * shipped here must be reached by a model slot.
 *
 * @see TestBPMNModeler
 */
@RunWith(OrderedRunner.class)
public class TestBPMNModelerFibs extends OpenflexoTestCase {

	protected static final Logger logger = Logger.getLogger(TestBPMNModelerFibs.class.getPackage().getName());

	private static FlexoResourceCenter<?> resourceCenter;

	@Test
	@TestOrder(1)
	public void test0InstantiateResourceCenter() {

		log("test0InstantiateResourceCenter()");

		instanciateTestServiceManager(EMFTechnologyAdapter.class, DiagramTechnologyAdapter.class, GINATechnologyAdapter.class);

		resourceCenter = serviceManager.getResourceCenterService().getFlexoResourceCenter(BPMNModelerConstants.BPMN_MODELER_RC_URI);
		assertNotNull("No resource center for " + BPMNModelerConstants.BPMN_MODELER_RC_URI, resourceCenter);
	}

	/**
	 * Every {@link FIBComponentModelSlot} declared in this resource center reaches a component that actually loads.
	 */
	@Test
	@TestOrder(2)
	public void test1EveryModelSlotReachesItsComponent() {

		log("test1EveryModelSlotReachesItsComponent()");

		Map<VirtualModel, FIBComponentModelSlot> slots = fibModelSlots();
		assertEquals("Unexpected set of VirtualModels declaring a FIBComponentModelSlot",
				new TreeSet<>(Arrays.asList("BPMNEditor", "BPMNModel")), names(slots.keySet()));

		for (Map.Entry<VirtualModel, FIBComponentModelSlot> entry : slots.entrySet()) {

			VirtualModel virtualModel = entry.getKey();
			FIBComponentModelSlot modelSlot = entry.getValue();

			String uri = modelSlot.getTemplateComponentURI();
			assertTrue("No templateComponentURI on the FIBComponentModelSlot of " + virtualModel.getName(),
					uri != null && uri.length() > 0);
			assertTrue("templateComponentURI of " + virtualModel.getName() + " leaves this resource center: " + uri,
					uri.startsWith(BPMNModelerConstants.BPMN_MODELER_RC_URI + "/"));

			// The silent failure this whole suite exists for: an unresolved URI costs the VirtualModel
			// its FML-controlled-FIB nature, with no error reported anywhere.
			assertNotNull("templateComponentURI of " + virtualModel.getName() + " resolves to nothing: " + uri,
					modelSlot.getTemplateResource());
			assertEquals(uri, modelSlot.getTemplateResource().getURI());

			GINAFIBComponent component = FMLControlledFIBVirtualModelNature.getFIBComponent(virtualModel.getDeclaringCompilationUnit());
			assertNotNull("No GINAFIBComponent behind " + uri, component);
			assertNotNull("The .fib behind " + uri + " holds no FIBComponent", component.getComponent());

			assertTrue(virtualModel.getName() + " has lost its FML-controlled-FIB nature",
					FMLControlledFIBVirtualModelNature.INSTANCE.hasNature(virtualModel));
		}
	}

	/**
	 * And the other way round: no <code>.fib</code> is shipped here without a VirtualModel federating it.
	 *
	 * <p>
	 * An orphan component is dead weight that nothing else would notice - the resource center loads it happily.
	 */
	@Test
	@TestOrder(3)
	public void test2NoOrphanComponent() {

		log("test2NoOrphanComponent()");

		TreeSet<String> federated = new TreeSet<>();
		for (FIBComponentModelSlot modelSlot : fibModelSlots().values()) {
			federated.add(modelSlot.getTemplateComponentURI());
		}

		TreeSet<String> shipped = new TreeSet<>();
		for (FlexoResource<?> resource : resourceCenter.getAllResources()) {
			if (resource instanceof GINAFIBComponentResource) {
				shipped.add(resource.getURI());
			}
		}

		assertEquals("A .fib of this resource center is federated by no VirtualModel", shipped, federated);
	}

	/**
	 * Every binding of every component is valid, once bound to the typing space of the VirtualModel that federates it.
	 *
	 * <p>
	 * {@link GINAFIBComponent#bindTo} is what installs that context - the FML binding factory, the technology-adapter type manager, and the
	 * type of the <code>data</code> variable taken from the model slot assignments. It is exactly what the module view does before showing
	 * the component, and it matters twice over: validating without it reports every binding on <code>data</code> as broken, and it also
	 * swaps the expression parser for the FML one, under which a binding that reads fine in the FIB editor may no longer parse at all.
	 *
	 * <p>
	 * These two components were 2012 user interfaces addressing a model the FML migration had not reconstituted, and this assertion was
	 * first written against a list of tolerated failures. That list is now empty: see the header of each <code>.fib</code>-facing behaviour
	 * in BPMNEditor and BPMNModel for what had to be declared, and the removed widgets for what had no BPMN counterpart at all.
	 */
	@Test
	@TestOrder(4)
	public void test3EveryBindingIsValid() throws InterruptedException {

		log("test3EveryBindingIsValid()");

		for (Map.Entry<VirtualModel, FIBComponentModelSlot> entry : fibModelSlots().entrySet()) {

			VirtualModel virtualModel = entry.getKey();
			FIBComponentModelSlot modelSlot = entry.getValue();
			String uri = modelSlot.getTemplateComponentURI();

			GINAFIBComponent component = FMLControlledFIBVirtualModelNature.getFIBComponent(virtualModel.getDeclaringCompilationUnit());
			component.bindTo(virtualModel, modelSlot);

			FIBComponent fibComponent = component.getComponent();
			ValidationReport report = fibComponent.validate();

			TreeSet<String> invalid = new TreeSet<>();
			for (ValidationError<?, ?> error : report.getAllErrors()) {
				invalid.add(report.getValidationModel().localizedIssueMessage(error));
			}

			System.out.println("Validated " + uri + ": " + invalid.size() + " invalid binding(s)");
			assertEquals("Invalid binding(s) in " + uri + ": " + invalid, new TreeSet<String>(), invalid);
		}
	}

	/**
	 * The {@link FIBComponentModelSlot} each VirtualModel of this resource center <em>declares</em>, indexed by VirtualModel.
	 */
	private Map<VirtualModel, FIBComponentModelSlot> fibModelSlots() {

		Map<VirtualModel, FIBComponentModelSlot> returned = new LinkedHashMap<>();

		for (FlexoResource<?> resource : resourceCenter.getAllResources()) {
			if (resource instanceof CompilationUnitResource) {
				VirtualModel virtualModel = ((CompilationUnitResource) resource).getCompilationUnit().getVirtualModel();
				if (virtualModel != null) {
					for (FIBComponentModelSlot modelSlot : virtualModel.getModelSlots(FIBComponentModelSlot.class)) {
						// getModelSlots() also reports what a CONTAINER declares, so ProcessDiagram would
						// answer with BPMNEditor's slot. Keep only what this VirtualModel declares itself.
						if (modelSlot.getFlexoConcept() == virtualModel) {
							returned.put(virtualModel, modelSlot);
						}
					}
				}
			}
		}
		return returned;
	}

	private static TreeSet<String> names(Iterable<VirtualModel> virtualModels) {
		TreeSet<String> returned = new TreeSet<>();
		for (VirtualModel virtualModel : virtualModels) {
			returned.add(virtualModel.getName());
		}
		return returned;
	}
}
