/**
 * Openflexo is a computer program whose purpose is to provide an open-source, free and
 * open-source model federation platform.
 */

package org.openflexo.modelers.bpmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.fib.binding.FMLControlledComponent;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.foundation.resource.FlexoResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.gina.model.FIBComponent;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.rm.Resource;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.emf.EMFTechnologyAdapter;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * The user interfaces of this modeler are GINA components stored in the <code>Xxx.fml/</code> container of the VirtualModel that drives
 * them, and resolved by naming convention - <code>BPMNEditor.fml/BPMNEditor.fib</code>,
 * <code>BPMNEditor.fml/BPMNModel.fml/BPMNModel.fib</code>.
 *
 * <p>
 * This replaced the <code>gina-ta</code> bridge, where the link was a <code>templateComponentURI</code> string on a
 * <code>FIBComponentModelSlot</code>. That link broke <b>silently</b>: an URI resolving to nothing simply made the VirtualModel lose its
 * nature, and the application showed an empty panel. The convention cannot break that way - the component either sits beside the FML source
 * or it does not - but this suite still walks the link in <b>both</b> directions, because an orphan component is dead weight nothing else
 * would notice.
 */
@RunWith(OrderedRunner.class)
public class TestBPMNModelerFibs extends OpenflexoTestCase {

	protected static final Logger logger = Logger.getLogger(TestBPMNModelerFibs.class.getPackage().getName());

	private static FlexoResourceCenter<?> resourceCenter;

	@Test
	@TestOrder(1)
	public void test0InstantiateResourceCenter() {

		log("test0InstantiateResourceCenter()");

		instanciateTestServiceManager(EMFTechnologyAdapter.class, DiagramTechnologyAdapter.class);

		resourceCenter = serviceManager.getResourceCenterService().getFlexoResourceCenter(BPMNModelerConstants.BPMN_MODELER_RC_URI);
		assertNotNull("No resource center for " + BPMNModelerConstants.BPMN_MODELER_RC_URI, resourceCenter);

	}

	/**
	 * The VirtualModels that ship a user interface are exactly the ones expected, and each reaches a component that actually loads.
	 */
	@Test
	@TestOrder(2)
	public void test1EveryDrivenVirtualModelReachesItsComponent() {

		log("test1EveryDrivenVirtualModelReachesItsComponent()");

		Map<VirtualModel, Resource> driven = drivenVirtualModels();

		assertEquals("Unexpected set of VirtualModels driving a user interface", new TreeSet<>(java.util.Arrays.asList("BPMNEditor",
				"BPMNModel")), names(driven.keySet()));

		for (Map.Entry<VirtualModel, Resource> entry : driven.entrySet()) {

			VirtualModel virtualModel = entry.getKey();

			// The component sits in the container of the VirtualModel, and is named after it
			assertTrue("The component of " + virtualModel.getName() + " is not named after it: " + entry.getValue().getRelativePath(),
					entry.getValue().getRelativePath().endsWith("/" + virtualModel.getName() + ".fib"));

			FIBComponent component = FMLControlledComponent.loadUIComponent(virtualModel, null);
			assertNotNull("The component of " + virtualModel.getName() + " does not load", component);

			// Loading installs the FML binding context - what the model slot's bindTo() used to do
			assertNotNull("The component of " + virtualModel.getName() + " was not bound to its concept",
					component.getVariable(org.openflexo.gina.model.FIBComponent.DEFAULT_DATA_VARIABLE));
		}
	}

	/**
	 * And the other way round: no <code>.fib</code> is shipped here without a VirtualModel driving it.
	 */
	@Test
	@TestOrder(3)
	public void test2NoOrphanComponent() {

		log("test2NoOrphanComponent()");

		TreeSet<String> driven = new TreeSet<>();
		for (Resource componentResource : drivenVirtualModels().values()) {
			driven.add(componentResource.getRelativePath());
		}

		TreeSet<String> shipped = new TreeSet<>();
		for (FlexoResource<?> resource : resourceCenter.getAllResources()) {
			if (resource instanceof CompilationUnitResource) {
				Resource container = ((CompilationUnitResource) resource).getDirectory();
				if (container != null) {
					for (Resource artefact : container.getContents(false)) {
						if (artefact.getRelativePath() != null && artefact.getRelativePath().endsWith(".fib")) {
							shipped.add(artefact.getRelativePath());
						}
					}
				}
			}
		}

		assertEquals("A .fib of this resource center is driven by no VirtualModel", shipped, driven);
	}

	/**
	 * Every binding of every component is valid, in the context the module view shows it in.
	 *
	 * <p>
	 * {@link FMLControlledComponent#loadUIComponent} is what installs that context - the FML binding factory and the type of the inspected
	 * instance. It matters twice over: validating without it reports every binding on <code>data</code> as broken, and it also swaps the
	 * expression parser for the FML one, under which a binding that reads fine in the FIB editor may no longer parse at all.
	 *
	 * <p>
	 * The tolerated-failure list is deliberately EMPTY, and was already empty before these components moved into their containers. Do not
	 * reintroduce one without saying, per entry, what decision is missing.
	 */
	@Test
	@TestOrder(4)
	public void test3EveryBindingIsValid() throws InterruptedException {

		log("test3EveryBindingIsValid()");

		for (Map.Entry<VirtualModel, Resource> entry : drivenVirtualModels().entrySet()) {

			VirtualModel virtualModel = entry.getKey();
			FIBComponent component = FMLControlledComponent.loadUIComponent(virtualModel, null);

			ValidationReport report = component.validate();

			TreeSet<String> invalid = new TreeSet<>();
			for (ValidationError<?, ?> error : report.getAllErrors()) {
				invalid.add(report.getValidationModel().localizedIssueMessage(error));
			}

			System.out.println("Validated " + entry.getValue().getRelativePath() + ": " + invalid.size() + " invalid binding(s)");
			assertEquals("Invalid binding(s) in " + entry.getValue().getRelativePath() + ": " + invalid, new TreeSet<String>(), invalid);
		}
	}

	/**
	 * The VirtualModels of this resource center that drive a user interface, indexed by VirtualModel.
	 *
	 * <p>
	 * Unlike the model-slot era, there is nothing to filter out here: a component is found in the container of the compilation unit that
	 * declares the concept, so a contained VirtualModel can no longer answer with its container's.
	 */
	private Map<VirtualModel, Resource> drivenVirtualModels() {

		Map<VirtualModel, Resource> returned = new LinkedHashMap<>();

		for (FlexoResource<?> resource : resourceCenter.getAllResources()) {
			if (resource instanceof CompilationUnitResource) {
				VirtualModel virtualModel = ((CompilationUnitResource) resource).getCompilationUnit().getVirtualModel();
				if (virtualModel != null) {
					Resource componentResource = virtualModel.getUIComponentResource();
					if (componentResource != null) {
						returned.put(virtualModel, componentResource);
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
