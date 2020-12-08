package bernardi;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SortingVisualization extends Application {
    final static int width = (int)(Screen.getPrimary().getBounds().getWidth() * 0.75); // global variable width
    final static int height =(int)(Screen.getPrimary().getBounds().getHeight() * 0.75);
    static Pane bottomPane = new Pane(); // global root
    static Queue<Moves> q;

    private static int n; // global number of elements to be sorted

    // The timeline should be declared global
    private static Timeline timeline = null;

    private static ArrayList<myRectangle> elements;
    private static ArrayList<myRectangle> elements2;


    @Override
    public void start(Stage primaryStage) throws InterruptedException {

        System.out.println(height);
        System.out.println(width);
        System.out.println(Screen.getPrimary().getBounds().getHeight());
        System.out.println(Screen.getPrimary().getBounds().getWidth());

        FlowPane topPane = new FlowPane();
        topPane.setStyle("-fx-background-color: #21c6ef;");

        // declare and style button
        Button button = new Button();
        button.setStyle("-fx-background-color: RED; -fx-font-size: 18");
        button.setText("ON/OFF");
        button.setPrefSize(150,75);
        button.setOnAction(event -> toggle());


        bottomPane.setPrefSize(width, height);

        // creates radio buttons and adds them to a toggle group for mutually exclusivity
        RadioButton rbBubble = new RadioButton("Bubble Sort");
        RadioButton rbSelection = new RadioButton("Selection Sort");
        RadioButton rbInsertion = new RadioButton("Insertion Sort");
        RadioButton rbQuickSort = new RadioButton("QuickSort");

        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(rbBubble, rbInsertion, rbQuickSort, rbSelection);

        for(Toggle t: tg.getToggles())
        {
            ((RadioButton)(t)).setStyle("-fx-font-size: 13;");
        }


        topPane.setOrientation(Orientation.HORIZONTAL);
        topPane.setAlignment(Pos.CENTER);
        topPane.setPrefWrapLength(1000);
        topPane.setPadding(new Insets(10));
        topPane.setHgap(30);

        // the root of the entire scene. Use VBox
        VBox root = new VBox();

        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);
        slider.showTickMarksProperty();
        slider.setShowTickLabels(true);
        slider.setTooltip(new Tooltip("Number of elements to be sorted."));
        slider.setShowTickMarks(true);
        slider.setValue(50);

        root.getChildren().add(topPane);
        root.getChildren().add(bottomPane);
        topPane.getChildren().addAll(slider, rbBubble, rbInsertion, rbSelection, rbQuickSort, button);
        root.setAlignment(Pos.CENTER);


        Scene scene = new Scene(root);
        scene.setFill(Color.web("21c6ef"));

        bottomPane.setStyle("-fx-background-color: #000000;");




        // this is needed just to initialize the timeline object as something
        // otherwise will throw exception in buttonHandler
        timeline = new Timeline();

        n = (int)slider.getValue();


        /**
         * Custom Nested class that implements the EventHandler Interface. Instances of this class are passed
         * to each radio button.
         */
        class rbButtonHandler implements EventHandler<ActionEvent> {
            @Override
            public void handle(ActionEvent event) {
                if(timeline.getStatus() == Animation.Status.RUNNING)
                {
                    timeline.stop();
                }
                n = (int)slider.getValue();
                elements = null;
                elements2 = null;
                elements = new ArrayList<>(); // this list is the one used to sort
                elements2 = new ArrayList<>(); // this list is used to display on Pane
                createRectangles(elements, elements2);
                initRectangles(elements2, bottomPane);

                if(rbBubble.isSelected())
                {
                    bubbleSort(elements);
                    setUpKeyFrame(false, true);
                }
                else if(rbInsertion.isSelected())
                {
                    insertionSort(elements);
                    setUpKeyFrame(false, false);
                }
                else if(rbSelection.isSelected())
                {
                    selectionSort(elements);
                    setUpKeyFrame(false, false);
                }
                else
                {
                    qsort(elements, 0, n-1);
                    setUpKeyFrame(true, false);
                }
            }
        }


        // pass buttonHandler into each radio button
        for(Toggle t: tg.getToggles())
        {
            ((RadioButton)(t)).setOnAction(new rbButtonHandler());
        }


        // boiler plate javafx code
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sorting...");
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    /**
     * Creates an ArrayList of bernardi.myRectangle objects with the width = width/n and a randomly
     * generated height. This height will serve as the "value" of the rectangle when the
     * elements array is eventually sorted.
     *
     * @return bernardi.myRectangle[]
     */
    public static void createRectangles(ArrayList<myRectangle> elements, ArrayList<myRectangle> elements2) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            // first create a randomly generated height between 0 and global height variable
            int randomHeight = rand.nextInt(0, height);
            elements.add(new myRectangle(randomHeight));
            elements2.add(new myRectangle(randomHeight));
            elements.get(i).setHeight(randomHeight);
            elements2.get(i).setHeight(randomHeight);
            elements.get(i).setWidth(width / n);
            elements2.get(i).setWidth(width / n);
        }
        System.out.println("Width = " + width);
        System.out.println("n = " + n);
        System.out.println("Rectangle width = " + elements2.get(0).getWidth());
        System.out.println("width/n = " + width/n );
        System.out.println("width - ((width/n) * n) = " + (width - ((width/n) * n)));
    }

    /**
     * Takes the visualization MyArrayList and colors it. Also sets the height and style border.
     * @param elements
     * @param pane
     */
    public static void initRectangles(ArrayList<myRectangle> elements, Pane pane) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        if(pane.getChildren().size() != 0)
        {
            pane.getChildren().clear();
        }

        q = null;
        q = new LinkedList<Moves>();
        for (int i = 0; i < n; i++) {
            pane.getChildren().add(elements.get(i));
            //elements.get(i).setFill(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            elements.get(i).setFill(getRandomPastelColor());
            elements.get(i).setY(height - elements.get(i).getHeight());
            elements.get(i).setX(i * (width / n));
            elements.get(i).setStyle("-fx-stroke: BLACK; -fx-stroke-width: 3; ");
        }
    }

    /**
     * Returns a randomly generated Color
     */
    private static Color getRandomPastelColor() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        int r = rand.nextInt(50, 256);
        int g = rand.nextInt(50, 256);
        int b = rand.nextInt(50, 256);
        return Color.rgb(r, g, b);
    }
    /**
     * Returns a randomly generated Color neither white or black
     */
    private static Color getDarkerColor()
    {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int r = rand.nextInt(10, 180);
        int g = rand.nextInt(10, 180);
        int b = rand.nextInt(10, 180);
        return Color.rgb(r, g, b);
    }

    /**
     * BubbleSort
     *
     * @param elements ArrayList<bernardi.myRectangle>
     */
    private static void bubbleSort(ArrayList<myRectangle> elements) {
        for (int j = 0; j < n - 1; j++) {
            for (int i = 0; i <= n - 2; i++) {
                if (elements.get(i).getValue() > elements.get(i + 1).getValue()) {
                    q.add(new Moves(i, i + 1));
                    swapRectangles(elements, i, i + 1);
                }
            }
        }
    }


    /**
     * selection sort
     */
    private static void selectionSort(ArrayList<myRectangle> elements) {
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i; j < n; j++) {
                if (elements.get(j).getValue() < elements.get(min).getValue()) {
                    min = j;
                }
            }
            q.add(new Moves(min, i)); // store the swap into queue
            swapRectangles(elements, min, i);
            //std::swap(a[min], a[i]);  c++ line
        }
    }

    private static void insertionSort(ArrayList<myRectangle> elements) {
        int j;
        for (int i = 0; i < n; i++) {
            j = i;
            //while(j > 0 && (a[j] < a[j-1]))
            while (j > 0 && elements.get(j).getValue() < elements.get(j - 1).getValue()) {
                //swap(a[j],a[j-1]);
                q.add(new Moves(j, j - 1)); // store the swap
                swapRectangles(elements, j, j - 1);
                j--;
            }
        }
    }

    /**
     * This function will take an array with lo and high indices. It will choose a pivot element, in this case the very
     * first element in the array, and partition the array such that all elements less than or equal to the pivot are on
     * the left of the the pivot, and all elements greater than are on the right of the pivot. It will also return the
     * new index of the pivot after partitioning. I am aware that I could have used a for-loop, however, this is what I
     * originally came up with immediately after our class on quick-sort and I have an emotional attachment to this
     * implementation. The infinite loop will always be broken out of it, as long as the preconditions of the specified
     * array and indices are valid. According to my calculations, which may be incorrect, the asymptotic complexity is
     * still O(n), so it meets the requirement. When used in a quick sort function, partition() is never called on
     * arrays that contain less than two elements in size. However, this partition will still work correctly on arrays
     * containing only one element.
     *
     * @return int the new index of the pivot after partitioning
     */
    private static int partition(ArrayList<myRectangle> elements, int lo, int hi) {
        // taking the pivot to be the first element in the array
        int pivot = elements.get(lo).getValue();
        //int pivot = a[lo];

        int i = lo; // left hand pointer starts at the index of the first element (the pivot)
        int j = hi; // right hand pointer starts at the index of the last element in the list

        while (true) {

            // increment left pointer until it reaches an element that is greater than pivot or it reaches the hi of the
            // array
            // while(a[i] <= pivot && i < hi)
            while (elements.get(i).getValue() <= pivot && i < hi) {
                i++;
            }
            // decrement right pointer until reaches an element that is smaller than pivot or it reaches the
            // left most element (first element to right of pivot)
            //while(a[j] > pivot && j > lo)
            while (elements.get(j).getValue() > pivot && j > lo) {
                j--;
            }

            // if the pointer indices cross each other, or they are equal, swap the pivot with a[j] and break out by
            // returning new index of pivot
            if (j <= i) {
                //std::swap(a[lo], a[j]);
                q.add(new Moves(lo, j)); // add swap move to queue
                swapRectangles(elements, lo, j);
                return j;
            }

            // at this point, the pointers did not meet, and arr[i] > pivot && arr[j] < pivot
            //std::swap(a[i], a[j]);
            q.add(new Moves(i, j)); // add swap move to queue
            swapRectangles(elements, i, j);
        }

    }

    /**
     * Standard recursive implementation of QuickSort.
     *
     * int[] a[] - An array of ints to be sorted
     * int lo - the starting index of the Array
     * int hi - The ending index of the Array
     */
    private static void qsort(ArrayList<myRectangle> elements, int lo, int hi) {
        if (lo < hi) {
            int k = partition(elements, lo, hi);
            q.add(new Moves(lo, hi, k,true));
            qsort(elements, lo, k - 1);
            qsort(elements, k + 1, hi);
        }
    }

    private static void splitColorsAroundPartition(ArrayList<myRectangle> elements, int lo, int hi, int k) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        Color c1 = getDarkerColor();
        Color c2 = getDarkerColor();
        for (int i = lo; i < k; i++) {
            elements.get(i).setFill(c1);
        }
        for (int j = k + 1; j <= hi; j++) {
            elements.get(j).setFill(c2);
        }
        elements.get(k).setFill(Color.WHITE);
    }

    /**
     * This method is used to swap rectangles in the visualized ArrayList. It will swap the position
     * of the Rectangles specified in the indicies in the bottomPane.getChildren() list.
     * @param elements
     * @param i
     * @param j
     */
    private static void swapRectangles(ArrayList<myRectangle> elements, int i, int j) {
        double temp = elements.get(i).getX();
        elements.get(i).setX(elements.get(j).getX());
        elements.get(j).setX(temp);
        Collections.swap(elements, i, j);
    }

    private static void toggle()
    {
        if(timeline.getStatus()== Animation.Status.RUNNING)
        {
            timeline.stop();
        }
        else
        {
            timeline.play();
        }
    }

    /**
     * This sets up the KeyFrame and Timeline objects to execute the code ever t milliseconds.
     * Each sorting algorithm looks best as a different time, so the type of algorithm used is specified
     * in the boolean parameters.
     * @param isQuickSort
     * @param isBubbleSort
     */
    private static void setUpKeyFrame(boolean isQuickSort, boolean isBubbleSort)
    {
        KeyFrame kf;
        if(isBubbleSort)
        {
             kf = new KeyFrame(Duration.millis(80), e -> {

                try {
                    Moves moves = q.remove();
                    int i = moves.getI();
                    int j = moves.getJ();
                    swapRectangles(elements2, i, j);
                } catch (Exception ex) {
                }

            }
            );

        }
        else if(isQuickSort)
        {
            kf = new KeyFrame(Duration.millis(250), e-> {
                try {
                    Moves moves = q.remove();
                    if (moves.getAfterPartition()) {
                        splitColorsAroundPartition(elements2, moves.getLo(), moves.getHi(), moves.getK());
                    } else {
                        int i = moves.getI();
                        int j = moves.getJ();
                        swapRectangles(elements2, i, j);
                    }
                } catch (Exception ex) {
                }
        }
        );

        }
        else {
            kf = new KeyFrame(Duration.millis(300), e -> {

                try {
                    Moves moves = q.remove();
                    int i = moves.getI();
                    int j = moves.getJ();
                    swapRectangles(elements2, i, j);
                } catch (Exception ex) {
                }

            }
            );

        }
        timeline = null;
        timeline = new Timeline(kf);
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
