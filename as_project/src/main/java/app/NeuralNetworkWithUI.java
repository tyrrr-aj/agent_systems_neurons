package app;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.network.EdgeDrawInfo2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;

public class NeuralNetworkWithUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D neuralNetworkContinuousPortrayal = new ContinuousPortrayal2D();
    NetworkPortrayal2D neuralNetworkNetworkPortrayal = new NetworkPortrayal2D();

    public NeuralNetworkWithUI(){
        super(new NeuralNetwork(System.currentTimeMillis()));
    }
    public NeuralNetworkWithUI(SimState simState){
        super(simState);
    }
    public static String getName(){
        return "Neural Network Simulation";
    }

    public static void main(String[] args){
        NeuralNetworkWithUI neuralNetworkWithUI = new NeuralNetworkWithUI();
        Console c = new Console(neuralNetworkWithUI);
        c.setVisible(true);
    }

    public void start(){
        super.start();
        setupPortrayals();
    }

    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }

    public void setupPortrayals(){
        NeuralNetwork neuralNetwork = (NeuralNetwork) state;
        final double scale = 5.0;
        final double size = 15;

        neuralNetworkContinuousPortrayal.setField(neuralNetwork.brain);
        neuralNetworkContinuousPortrayal.setPortrayalForClass(ReceptoryNeuron.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        BaseNeuronAgent baseNeuronAgent = (BaseNeuronAgent) object;

                                        double excitation = baseNeuronAgent.getExcitation();
                                        int color= (int) (excitation+1)*100;
                                        paint = new Color(color, 0,255-color);
                                        info.draw.width = size;
                                        info.draw.height = size;
                                        super.draw(object, graphics, info);
                                    }
                                }, scale, null, Color.black, true),
                        0, scale, Color.green, true)));
        neuralNetworkContinuousPortrayal.setPortrayalForClass(ObjectNeuron.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        BaseNeuronAgent baseNeuronAgent = (BaseNeuronAgent) object;

                                        double excitation = baseNeuronAgent.getExcitation();
                                        int color= (int) (excitation+1)*100;
                                        paint = new Color(color, 0,255-color);
                                        info.draw.width = size;
                                        info.draw.height = size;
                                        super.draw(object, graphics, info);
                                    }
                                }, scale, null, Color.black, true),
                        0, scale, Color.green, true)));

        neuralNetworkContinuousPortrayal.setPortrayalForClass(Sensor.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        Sensor sensor = (Sensor) object;

                                        int color= (int) (sensor.getStimulation() * 255);
                                        paint = new Color(color, 0,255-color);
                                        info.draw.width = size;
                                        info.draw.height = size;
                                        super.draw(object, graphics, info);
                                    }
                                }, scale, null, Color.black, true),
                        0, scale, Color.green, true)));

        neuralNetworkContinuousPortrayal.setPortrayalForClass(ReceptoryField.class, new MovablePortrayal2D(
                new CircledPortrayal2D(
                        new LabelledPortrayal2D(
                                new OvalPortrayal2D() {
                                    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                                        ReceptoryField receptoryField = (ReceptoryField) object;

                                        int color= receptoryField.isStimulated() ? 255 : 0;
                                        paint = new Color(color, 0,255-color);
                                        info.draw.width = size;
                                        info.draw.height = size;
                                        super.draw(object, graphics, info);
                                    }
                                }, scale, null, Color.black, true),
                        0, scale, Color.green, true)));

        neuralNetworkNetworkPortrayal.setField(new SpatialNetwork2D(neuralNetwork.brain, neuralNetwork.network));
        SimpleEdgePortrayal2D p = new SimpleEdgePortrayal2D(Color.black, Color.black, Color.black, new Font("SansSerif", 0, 2)) {
            @Override
            public String getLabel(Edge edge, EdgeDrawInfo2D info) {
                return edge.getWeight() != 1.0 ? String.format("%.2f", edge.getWeight()) : "";
            }
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                if (!(info instanceof EdgeDrawInfo2D)) {
                    throw new RuntimeException("Expected this to be an EdgeDrawInfo2D: " + info);
                }
                else {
                    Edge edge = (Edge) object;

                    fromPaint = Color.black;
                    toPaint = Color.black;
                    int redColor = (int) Math.round((255 * edge.getWeight()) % 256);
                    if (BaseNeuronAgent.class.isAssignableFrom(edge.getTo().getClass())){
                        BaseNeuronAgent baseNeuronAgent = (BaseNeuronAgent) edge.getTo();
                        if (baseNeuronAgent.getExcitation() >= Constants.THRESHOLD)
                            fromPaint = new Color(redColor, 0, 0);
                    }
                    if (BaseNeuronAgent.class.isAssignableFrom(edge.getFrom().getClass())){
                        BaseNeuronAgent baseNeuronAgent = (BaseNeuronAgent) edge.getFrom();
                        if (baseNeuronAgent.getExcitation() >= Constants.THRESHOLD)
                            fromPaint = new Color(redColor, 0, 0);
                    }


                    EdgeDrawInfo2D e = (EdgeDrawInfo2D)info;
                    Line2D.Double preciseLine = new Line2D.Double();
                    double startXd = e.draw.x;
                    double startYd = e.draw.y;
                    double endXd = e.secondPoint.x;
                    double endYd = e.secondPoint.y;
                    double midXd = (startXd + endXd) / 2.0D;
                    double midYd = (startYd + endYd) / 2.0D;
                    int midX = (int)midXd;
                    int midY = (int)midYd;
                    double width;
                    double scale;
                    double weight;
                    Stroke oldstroke;
                    graphics.setPaint(this.fromPaint);
                    width = this.getBaseWidth();
                    scale = info.draw.width;
                    if (scale >= 1.0D)
                        scale = 1.0D;

                    oldstroke = graphics.getStroke();
                    weight = this.getPositiveWeight(object, e);
                    graphics.setStroke(new BasicStroke((float)(width * weight * scale), this.shape == 1 ? 1 : (this.shape == 2 ? 2 : 0), 0));
                    preciseLine.setLine(startXd, startYd, endXd, endYd);
                    graphics.draw(preciseLine);
                    graphics.setStroke(oldstroke);

                    graphics.setPaint(this.fromPaint);
                    width = this.getBaseWidth();
                    scale = info.draw.width;
                    if (scale >= 1.0D)
                        scale = 1.0D;

                    oldstroke = graphics.getStroke();
                    weight = this.getPositiveWeight(object, e);
                    graphics.setStroke(new BasicStroke((float)(width * weight * scale), this.shape == 1 ? 1 : (this.shape == 2 ? 2 : 0), 0));
                    preciseLine.setLine(startXd, startYd, midXd, midYd);
                    graphics.setPaint(fromPaint);
                    graphics.draw(preciseLine);
//                    graphics.setPaint(toPaint);
                    preciseLine.setLine(midXd, midYd, endXd, endYd);
                    graphics.draw(preciseLine);
                    graphics.setStroke(oldstroke);

                    if (this.labelPaint != null) {
                        Font labelFont = this.labelFont;
                        String information = this.getLabel((Edge)object, e);
                        if (information.length() > 0) {
                            graphics.setPaint(this.labelPaint);
                            graphics.setFont(labelFont);
                            width = graphics.getFontMetrics().stringWidth(information);
                            graphics.drawString(information, (float) (midX - width / 2), midY);
                        }
                    }

                }
            }
        };
        p.setAdjustsThickness(true);
        p.setBaseWidth(1.5);
        neuralNetworkNetworkPortrayal.setPortrayalForAll(p);

        display.reset();
        display.setBackdrop(Color.WHITE);

        display.repaint();
    }

    public void init(Controller c){
        super.init(c);
        display = new Display2D(600,600,this);
        display.setClipping(false);

        displayFrame = display.createFrame();
        displayFrame.setTitle("Neural Network Display");
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(neuralNetworkNetworkPortrayal, "Network");
        display.attach(neuralNetworkContinuousPortrayal, "Neural Network");
    }

    public void quit(){
        super.quit();
        if (displayFrame!=null)
            displayFrame.dispose();
        displayFrame=null;
        display=null;
    }

    public Object getSimulationInspectedObject() {
        return state;
    }

    public Inspector getInspector(){
        Inspector inspector = super.getInspector();
        inspector.setVolatile(true);
        return inspector;
    }

}
