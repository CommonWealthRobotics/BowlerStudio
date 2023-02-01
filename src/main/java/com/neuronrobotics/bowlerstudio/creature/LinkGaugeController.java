package com.neuronrobotics.bowlerstudio.creature;

import com.neuronrobotics.bowlerstudio.BowlerStudio;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.ILinkConfigurationChangeListener;
import com.neuronrobotics.sdk.addons.kinematics.ILinkListener;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.TickLabelLocation;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.TickMarkType;
import eu.hansolo.medusa.Gauge.KnobType;
import eu.hansolo.medusa.Gauge.NeedleShape;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;

public class LinkGaugeController implements ILinkListener, ILinkConfigurationChangeListener {
	private int SIZE_OF_GAUGE = 200;
	private Gauge gauge;
	private Section bounds;
	private Section boundsPossible;
	private LinkConfiguration conf;
	private AbstractLink link;
	private boolean isNowVis=false;
	public Gauge getGauge() {
		if (gauge == null) {
			double spread = 60;
			bounds = new Section(0, 0, Color.rgb(60, 130, 145, 0.7));
			boundsPossible = new Section(0, 0, Color.ORANGE);
			
			gauge = GaugeBuilder
					.create()
					.decimals(2)
					.foregroundBaseColor(Color.BLACK)
					.prefSize(getSIZE(), getSIZE())
					.startAngle(360 - (spread / 2))
					.angleRange(360 - spread)
					.minValue(-180 + (spread / 2))
					.maxValue(180 - (spread / 2))
					.tickLabelLocation(TickLabelLocation.OUTSIDE)
					.tickLabelOrientation(TickLabelOrientation.ORTHOGONAL)
					.minorTickMarksVisible(false)
					//.tickLabelsVisible(false)
					.majorTickMarkType(TickMarkType.BOX)
					.valueVisible(true)
					.knobType(KnobType.FLAT)
					.needleShape(NeedleShape.FLAT)
					.needleColor(Color.RED)
					.tickLabelsVisible(true)
					.sectionsVisible(true)
					.sections(boundsPossible, bounds)
					.build();
			BowlerStudio.runLater(() -> {
				gauge.setInteractive(false);
				gauge.setTitle("");
				turnOffPickOnBoundsFor(gauge);
			});
			gauge.parentProperty().addListener((observable, oldValue, newValue) ->        {
			    isNowVis=newValue!=null;
			    if(isNowVis) {
			    	event(conf);
			    	gauge.setValue(link.getCurrentEngineeringUnits());
			    }
			});
			
		}
		return gauge;
	}

	private boolean turnOffPickOnBoundsFor(Node n) {
		boolean result = false;
		n.setPickOnBounds(false);
		if (n instanceof Parent) {
			for (Node c : ((Parent) n).getChildrenUnmodifiable()) {
				if (turnOffPickOnBoundsFor(c)) {
					result = true;
				}
			}
		}
		n.setMouseTransparent(!result);
		return result;
	}

	public void setLink(LinkConfiguration lf, AbstractLink l) {
		if (link != null)
			link.removeLinkListener(this);
		this.link = l;
		if (conf != null)
			conf.removeChangeListener(this);
		this.conf = lf;
		conf.addChangeListener(this);
		link.addLinkListener(this);
		event(conf);
	}

	private AbstractLink getAbstractLink() {
		return link;
	}

	@Override
	public void event(LinkConfiguration newConf) {
		if(!isNowVis||getAbstractLink()==null)
			return;
		BowlerStudio.runLater(() -> {
			bounds.setStart(getAbstractLink().getMinEngineeringUnits());
			bounds.setStop(getAbstractLink().getMaxEngineeringUnits());
			boundsPossible.setStart(getAbstractLink().getDeviceMinEngineeringUnits());
			boundsPossible.setStop(getAbstractLink().getDeviceMaxEngineeringUnits());
			gauge.setTitle("Link Bounds");
		});
	}

	@Override
	public void onLinkPositionUpdate(AbstractLink source, double engineeringUnitsValue) {
		if(!isNowVis)
			return;
		BowlerStudio.runLater(() -> gauge.setValue(engineeringUnitsValue));
	}

	@Override
	public void onLinkLimit(AbstractLink source, PIDLimitEvent event) {
	}

	public int getSIZE() {
		return SIZE_OF_GAUGE;
	}

	public void setSIZE(int sIZE_OF_GAUGE) {
		SIZE_OF_GAUGE = sIZE_OF_GAUGE;
		getGauge().setPrefSize(getSIZE(), getSIZE());
	}

}
