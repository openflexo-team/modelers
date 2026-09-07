package org.openflexo.modelers.bpmn;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.cli.CommandInterpreter;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.foundation.fml.cli.command.FMLScript;
import org.openflexo.foundation.fml.cli.command.fml.FMLAssertException;
import org.openflexo.foundation.fml.cli.test.FMLScriptParserTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.rm.Resources;
import org.openflexo.technologyadapter.diagram.DiagramTechnologyAdapter;
import org.openflexo.technologyadapter.emf.EMFTechnologyAdapter;

/**
 * A parameterized suite of FML-script driven integration tests for the BPMN modeler.
 *
 * <p>
 * Each {@code .fmlscript} under {@code src/main/resources/AutomatedTests} is parsed and executed; every {@code assert} it contains must
 * succeed. What is validated here is the modeler actually RUNNING - one .bpmn file federated, its process reified as concept instances, its
 * BPMN DI laid out on a diagram - not merely the fact that the three VirtualModels parse and validate, which is what
 * {@link TestBPMNModeler} covers.
 *
 * <p>
 * The scripts create their instances in a temporary resource center ({@code service ResourceCenterService add_temp_rc}), so that the
 * diagrams {@code ProcessDiagram} generates land there rather than in {@code src/main/resources}.
 *
 * @see TestBPMNModeler
 */
@RunWith(Parameterized.class)
public class BPMNModelerAutomatedTests extends FMLScriptParserTestCase {

	@Parameterized.Parameters(name = "{1}")
	public static Collection<Object[]> generateData() {
		return Resources.getMatchingResource(ResourceLocator.locateResource("AutomatedTests"), ".fmlscript");
	}

	private final Resource fmlResource;
	private FlexoEditor editor;
	private FMLScript script;
	private CommandInterpreter commandInterpreter;

	public BPMNModelerAutomatedTests(Resource fmlResource, String name) throws ParseException, ModelDefinitionException, IOException {
		System.out.println("********* Launch FML-script " + fmlResource + " name=" + name);
		this.fmlResource = fmlResource;
		initServiceManager();
	}

	@Test
	public void checkScript() throws ModelDefinitionException, ParseException, IOException, FMLCommandExecutionException {
		System.out.println("Parse script " + fmlResource.getRelativePath());
		script = parseFMLScript(fmlResource, commandInterpreter);
		checkFMLScript(fmlResource.getRelativePath(), script);
		try {
			script.execute();
		} catch (FMLAssertException e) {
			fail(e.getMessage());
		}
	}

	public void initServiceManager() throws ParseException, ModelDefinitionException, IOException {
		instanciateTestServiceManager(EMFTechnologyAdapter.class, DiagramTechnologyAdapter.class);

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(editor);

		commandInterpreter = new CommandInterpreter(serviceManager, System.in, System.out, System.err, HOME_DIR);
	}

}
