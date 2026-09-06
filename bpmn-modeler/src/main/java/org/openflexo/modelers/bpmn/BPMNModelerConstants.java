package org.openflexo.modelers.bpmn;

/**
 * The URIs published by the BPMN modeler resource center.
 *
 * <p>
 * These used to live in <code>org.openflexo.modelers.ModelersConstants</code>, under the
 * <code>http://openflexo.org/modellers</code> resource center shared by the four modelers. The BPMN modeler now carries its own resource
 * center, hence its own URI space.
 */
public class BPMNModelerConstants {

	/** Base URI of the BPMN modeler resource center, as declared by META-INF/resourceCenters. */
	public static final String BPMN_MODELER_RC_URI = "http://openflexo.org/bpmn-modeler";

	/** The editor itself: federates one .bpmn EMF model and holds one ProcessDiagram per process. */
	public static final String BPMN_EDITOR_URI = BPMN_MODELER_RC_URI + "/FML/BPMNEditor.fml";

	/** The abstract syntax of a BPMN process, reified over the EMF model. */
	public static final String BPMN_MODEL_URI = BPMN_EDITOR_URI + "/BPMNModel.fml";

	/** The diagram view of one BPMN process. */
	public static final String PROCESS_DIAGRAM_URI = BPMN_EDITOR_URI + "/ProcessDiagram.fml";

	/** The diagram specification the ProcessDiagram VirtualModel is typed by. */
	public static final String PROCESS_DIAGRAM_SPECIFICATION_URI = BPMN_MODELER_RC_URI
			+ "/DiagramSpecs/ProcessDiagram.diagramspecification";

	/** A small .bpmn model shipped with the modeler: one process, two tasks, one start and one end event. */
	public static final String BASIC_EXAMPLE_MODEL_URI = BPMN_MODELER_RC_URI + "/Models/BasicExample.bpmn";

}
