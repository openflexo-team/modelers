package org.openflexo.modelers.statecharts;

/**
 * The URIs published by the StateCharts modeler resource center.
 *
 * <p>
 * These used to live in the <code>http://openflexo.org/modellers</code> resource center shared by the four modelers. The StateCharts modeler
 * now carries its own resource center, hence its own URI space.
 */
public class StateChartsModelerConstants {

	/** Base URI of the StateCharts modeler resource center, as declared by META-INF/resourceCenters. */
	public static final String STATECHARTS_MODELER_RC_URI = "http://openflexo.org/statecharts-modeler";

	/** The container view point: it declares no concept, it only knows how to create a new state chart. */
	public static final String STATECHARTS_URI = STATECHARTS_MODELER_RC_URI + "/FML/StateCharts.fml";

	/** The abstract syntax: states and the transitions between them. */
	public static final String STATECHART_MODEL_URI = STATECHARTS_URI + "/StateChartModel.fml";

	/** The diagram view of a StateChartModel. */
	public static final String STATECHART_DIAGRAM_URI = STATECHARTS_URI + "/StateChartDiagram.fml";

	/** The diagram specification the StateChartDiagram VirtualModel is typed by. */
	public static final String STATECHART_DIAGRAM_SPECIFICATION_URI = STATECHARTS_MODELER_RC_URI
			+ "/DiagramSpecs/StateChartDiagramSpec.diagramspecification";

}
