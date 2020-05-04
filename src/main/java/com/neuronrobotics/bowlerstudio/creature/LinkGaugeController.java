package com.neuronrobotics.bowlerstudio.creature;

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
import javafx.scene.paint.Color;

public class LinkGaugeController implements ILinkListener, ILinkConfigurationChangeListener {
	private int SIZE_OF_GAUGE = 200;
	private Gauge gauge;
	private Section bounds;
	private Section boundsPossible;
	private LinkConfiguration conf;
	private AbstractLink link;

	public Gauge getGauge() {
		if (gauge == null) {
			double spread = 0;
			bounds = new Section(0, 0, Color.rgb(60, 130, 145, 0.7));
			boundsPossible = new Section(0, 0, Color.ORANGE);
			gauge = GaugeBuilder.create().foregroundBaseColor(Color.BLACK).prefSize(getSIZE(), getSIZE())
					.startAngle(360 - (spread / 2)).angleRange(360 - spread).minValue(-180 + (spread / 2))
					.maxValue(180 - (spread / 2)).tickLabelLocation(TickLabelLocation.OUTSIDE)
					.tickLabelOrientation(TickLabelOrientation.ORTHOGONAL).minorTickMarksVisible(false)
					.majorTickMarkType(TickMarkType.BOX).valueVisible(true).knobType(KnobType.FLAT)
					.needleShape(NeedleShape.FLAT).needleColor(Color.RED).sectionsVisible(true)
					.sections(boundsPossible, bounds).tickLabelsVisible(false).decimals(2).build();
		}
		return gauge;
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
		double rANGE = getAbstractLink().getMaxEngineeringUnits() - getAbstractLink().getMinEngineeringUnits();
		double theoreticalRange = getAbstractLink().getDeviceMaxEngineeringUnits()
				- getAbstractLink().getDeviceMinEngineeringUnits();
		Platform.runLater(() -> {
			bounds.setStart(getAbstractLink().getMinEngineeringUnits());
			bounds.setStop(getAbstractLink().getMaxEngineeringUnits());
			boundsPossible.setStart(getAbstractLink().getDeviceMinEngineeringUnits());
			boundsPossible.setStop(getAbstractLink().getDeviceMaxEngineeringUnits());
			gauge.setTitle("Link Range " + String.format("%.2f", rANGE) + "\nOf Possible "
					+ String.format("%.2f", theoreticalRange));
		});
	}

	@Override
	public void onLinkPositionUpdate(AbstractLink source, double engineeringUnitsValue) {
		Platform.runLater(() -> gauge.setValue(engineeringUnitsValue));
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
		getGauge().relocate(-SIZE_OF_GAUGE/2, -SIZE_OF_GAUGE/2);
	}

}
