package demos;

import camera.QueasyCam;
import math.Vec3;
import tools.Graph;
import physical.SphericalAgent;
import physical.SphericalObstacle;
import tools.Vertex;
import tools.configurationspace.BSHConfigurationSpace;
import tools.configurationspace.PlainConfigurationSpace;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

public class BSHSpeedUp extends PApplet {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 800;
    public static final int SIDE = 100;

    final Vec3 startPosition = Vec3.of(0, SIDE * (9f / 10), SIDE * (-9f / 10));
    final Vec3 finishPosition = Vec3.of(0, SIDE * (-9f / 10), SIDE * (9f / 10));
    SphericalAgent sphericalAgent;
    List<SphericalObstacle> sphericalObstacles = new ArrayList<>();
    PlainConfigurationSpace configurationSpace;
    Graph graph;

    QueasyCam cam;

    static boolean DRAW_OBSTACLES = true;

    public void settings() {
        size(WIDTH, HEIGHT, P3D);
    }

    public void setup() {
        surface.setTitle("Processing");
        colorMode(RGB, 1.0f);
        rectMode(CENTER);
        noStroke();

        cam = new QueasyCam(this);
        int numObstacles = 3000;
        for (int i = 0; i < numObstacles; i++) {
            sphericalObstacles.add(new SphericalObstacle(
                    this,
                    Vec3.of(0, -SIDE + i * SIDE * random(1.7f) / numObstacles, -SIDE + i * SIDE * random(1f) / numObstacles),
                    SIDE * (0.1f / 20),
                    Vec3.of(1, 0, 0)
            ));
        }
        sphericalAgent = new SphericalAgent(
                this,
                startPosition,
                20f,
                SIDE * (0.5f / 20),
                Vec3.of(1)
        );
        long startConfig = millis();
        configurationSpace = new PlainConfigurationSpace(this, sphericalAgent, sphericalObstacles);
        long configSpace = millis();
        PApplet.println("Time for config space creation = " + (configSpace - startConfig) + "ms");
        reset();
    }

    private void reset() {
        // vertex sampling
        long start = millis();
        List<Vec3> vertexPositions = new ArrayList<>();
        for (int i = 0; i < 10000; ++i) {
            vertexPositions.add(Vec3.of(0, random(-SIDE, SIDE), random(-SIDE, SIDE)));
        }
        graph = new Graph(this, startPosition, finishPosition);
        long sampling = millis();
        graph.generateVertices(vertexPositions, configurationSpace);
        long vertex = millis();
        graph.generateAdjacencies(10, configurationSpace);
        long edge = millis();
        PApplet.println("Time for vertex sampling = " + (sampling - start) + "ms");
        PApplet.println("Time for vertex culling = " + (vertex - sampling) + "ms");
        PApplet.println("Time for edge culling = " + (edge - vertex) + "ms");
    }

    public void draw() {
        if (keyPressed) {
            if (keyCode == RIGHT) {
                sphericalAgent.stepForward();
            }
            if (keyCode == LEFT) {
                sphericalAgent.stepBackward();
            }
        }
        long start = millis();
        // update
        sphericalAgent.update(0.1f);
        long update = millis();
        // draw
        background(0);
        // agent
        sphericalAgent.draw();
        // obstacles
        if (DRAW_OBSTACLES) {
            for (SphericalObstacle sphericalObstacle : sphericalObstacles) {
                sphericalObstacle.draw();
            }
        }
        // configuration space
        configurationSpace.draw();
        // graph
        graph.draw();
        long draw = millis();

        surface.setTitle("Processing - FPS: " + Math.round(frameRate) + " Update: " + (update - start) + "ms Draw " + (draw - update) + "ms");
    }

    public void keyPressed() {
        if (key == 'r') {
            reset();
        }
        if (key == 'g') {
            BSHConfigurationSpace.DRAW_BOUNDING_SPHERES = !BSHConfigurationSpace.DRAW_BOUNDING_SPHERES;
        }
        if (key == 'h') {
            DRAW_OBSTACLES = !DRAW_OBSTACLES;
        }
        if (key == 'k') {
            Graph.DRAW_VERTICES = !Graph.DRAW_VERTICES;
        }
        if (key == 'j') {
            Vertex.DRAW_EDGES = !Vertex.DRAW_EDGES;
        }
        if (key == 'p') {
            sphericalAgent.isPaused = !sphericalAgent.isPaused;
        }
        if (key == '1') {
            sphericalAgent.setPath(graph.dfs());
        }
        if (key == '2') {
            sphericalAgent.setPath(graph.bfs());
        }
        if (key == '3') {
            sphericalAgent.setPath(graph.ucs());
        }
        if (key == '4') {
            sphericalAgent.setPath(graph.aStar());
        }
        if (key == '5') {
            sphericalAgent.setPath(graph.weightedAStar(1.5f));
        }
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"demos.BSHSpeedUp"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }
}
