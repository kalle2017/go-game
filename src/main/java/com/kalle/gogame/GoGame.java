package com.kalle.gogame;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiexincong
 * @since 2022/11/11
 */
public class GoGame extends Application {

    private WavPlayer wavPlayer;

    @Override
    public void init() {
        this.wavPlayer = new WavPlayer();
    }

    // 落子颜色

    private static final Color[] COLOR_STATUS_MACHINE = {
            Color.WHITE,
            Color.BLACK
    };

    // 棋盘棋子数组

    private GoItem[][] goItems;

    // 当前步数
    private final AtomicInteger globalCurrentStep = new AtomicInteger(0);

    // 操作记录

    private final Stack<GoRecord> doLog = new Stack<>();

    // 后退记录

    private final Stack<GoRecord> undoLog = new Stack<>();

    @Override
    public void start(Stage stage) {

        // 设置关闭事件
        stage.setOnCloseRequest(e -> System.exit(0));

        Group box = new Group();

        Scene scene = new Scene(box, 800, 640);
        scene.setFill(Color.rgb(216, 178, 133));
        stage.setResizable(false);

        int road = 19, dest = 30;
        int x0 = 50, y0 = 50;

        // 绘制棋盘
        this.drawGoTable(road, x0, y0, dest, box);

        // 初始化棋盘
        this.initGoItems(road, x0, y0, dest, box);

        // 增加操作按钮
        this.addOperationListener(box);

        // 显示窗口
        stage.setTitle("GoGame v1.0");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 添加操作按钮
     *
     * @param box box
     */
    private void addOperationListener(Group box) {
        // 增加重新开始按钮
        Button restartButton = new Button("重新开始");
        restartButton.setPrefSize(100, 30);
        restartButton.setBorder(Border.EMPTY);
        restartButton.setLayoutX(650);
        restartButton.setLayoutY(60);

        restartButton.setOnMouseClicked(e -> reset());

        // 增加悔棋按钮
        Button cancelButton = new Button("悔棋");
        cancelButton.setPrefSize(100, 30);
        cancelButton.setBorder(Border.EMPTY);
        cancelButton.setLayoutX(650);
        cancelButton.setLayoutY(120);

        cancelButton.setOnMouseClicked(e -> cancel());

        // 添加到容器中
        box.getChildren().add(restartButton);
        box.getChildren().add(cancelButton);
    }

    /**
     * 初始化棋盘
     *
     * @param road 围棋路数
     * @param x0   横坐标偏移量
     * @param y0   纵坐标偏移量
     * @param dest 线距
     * @param box  容器
     */
    private void initGoItems(int road, int x0, int y0, int dest, Group box) {
        // 初始化棋盘
        goItems = new GoItem[road][road];

        for (int y = 0; y < road; y++) {
            for (int x = 0; x < road; x++) {
                GoItem circle = new GoItem(x, y, this);
                goItems[y][x] = circle;

                circle.setRadius(13);
                circle.setCenterX(x0 + x * dest);
                circle.setCenterY(y0 + y * dest);
                circle.setFill(Color.TRANSPARENT);

                // 上下
                if (y > 0) {
                    circle.setUp(goItems[y - 1][x]);
                    // 下自己设置不了，需要上一个去设置
                    goItems[y - 1][x].setDown(circle);
                }
                // 左右
                if (x > 0) {
                    circle.setLeft(goItems[y][x - 1]);
                    goItems[y][x - 1].setRight(circle);
                }

                // 点击事件
                circle.setOnMouseClicked(e -> {

                    // 落子音效
                    wavPlayer.play();

                    if (circle.getColor() == null) {
                        int nextStep = nextStep();
                        int status = nextStep % 2;

                        // 设置棋子颜色，目的是后面判断杀气的时候，判断气的数量
                        circle.setColor(COLOR_STATUS_MACHINE[status]);

                        // 吃子判断
                        // 遍历上下左右的棋子，判断是否
                        // 判断是否不入气，从当前棋子出发，是否没有气
                        GetBreathResult result = getBreaths(circle);

                        // 判断当前棋子的上下左右，是否存在不同颜色的棋子
                        Set<GoItem> killGoItems = new HashSet<>();
                        List<GoItem> directionGoItems = Arrays.asList(circle.getUp(), circle.getDown(), circle.getLeft(), circle.getRight());
                        for (GoItem directionGoItem : directionGoItems) {
                            killGoItems.addAll(findKillGoItems(circle.getColor(), directionGoItem));
                        }

                        if (result.getBreaths() == 0) {
                            // 不入气
                            if (killGoItems.isEmpty()) {
                                Toast.toast("不入气");
                                rollbackStep(circle);
                                return;
                            }

                            // TODO 打劫的情况
                            if (!doLog.isEmpty()) {
                                GoRecord peek = doLog.peek();
                                // 上一手刚好只吃掉一个子，而且是自己，则打劫
                                if (peek.getKillGoItems().size() == 1 && peek.getKillGoItems().contains(circle)) {
                                    Toast.toast("打劫");
                                    return;
                                }
                            }
                        }

                        for (GoItem killGoItem : killGoItems) {
                            killGoItem.setColor(null);
                        }

                        // 吃子音效
                        wavPlayer.remove(killGoItems.size());

                        // 每成功走一步，那么就将当前结果压入历史栈
                        GoRecord record = new GoRecord(circle.getColor(), circle, killGoItems);
                        doLog.push(record);
                    }
                });

                box.getChildren().add(circle);
            }
        }
    }

    /**
     * 绘制棋盘
     *
     * @param road 围棋路数
     * @param x0   横坐标偏移量
     * @param y0   纵坐标偏移量
     * @param dest 线距
     * @param box  容器
     */
    private void drawGoTable(int road, int x0, int y0, int dest, Group box) {
        // 画横竖线
        // 生成按钮
        int maxDest = (road - 1) * dest;

        for (int i = 0; i < road; i++) {
            // 横线
            Line lineH = new Line();

            lineH.setStartX(x0);
            lineH.setStartY(y0 + i * dest);
            lineH.setEndX(x0 + maxDest);
            lineH.setEndY(y0 + i * dest);

            // 竖线
            Line lineV = new Line();

            lineV.setStartX(x0 + i * dest);
            lineV.setStartY(y0);
            lineV.setEndX(x0 + i * dest);
            lineV.setEndY(y0 + maxDest);

            box.getChildren().add(lineH);
            box.getChildren().add(lineV);

            // 围棋上的小圆点
            Circle circle = new Circle();
            circle.setRadius(4);
            circle.setCenterX(x0 + 3 * dest + (i % 3) * 6 * dest);
            circle.setCenterY(y0 + 3 * dest + Math.floorDiv(i, 3) * 6 * dest);
            box.getChildren().add(circle);
        }
    }

    public void cancel() {
        if (doLog.isEmpty()) {
            Toast.toast("没有历史记录");
            return;
        }

        globalCurrentStep.decrementAndGet();
        GoRecord pop = doLog.pop();

        // 还原操作
        pop.getKiller().setColor(null);
        // 若上一步有吃子，那么此时需要全部还原
        if (!pop.getKillGoItems().isEmpty()) {
            Color reverseColor = getReverseColor(pop.getColor());
            for (GoItem goItem : pop.getKillGoItems()) {
                goItem.setColor(reverseColor);
            }
        }

        undoLog.push(pop);
    }

    private Color getReverseColor(Color color) {
        if (color == Color.BLACK) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

    public void reset() {
        globalCurrentStep.set(0);

        for (GoItem[] goItemRow : goItems) {
            for (GoItem goItem : goItemRow) {
                goItem.setColor(null);
            }
        }
    }

    private Set<GoItem> findKillGoItems(Color color, GoItem goItem) {
        Set<GoItem> killGoItems = new HashSet<>();
        if (goItem != null && goItem.getColor() != null && goItem.getColor() != color) {
            GetBreathResult breathResult = getBreaths(goItem);
            if (breathResult.getBreaths() == 0) {
                killGoItems.addAll(breathResult.getGroupItems());
            }
        }
        return killGoItems;
    }

    private GetBreathResult getBreaths(GoItem circle) {
        Color color = circle.getColor();

        boolean[][] visited = new boolean[goItems.length][goItems.length];
        List<GoItem> groupItems = new ArrayList<>();

        // 需要加入当前棋子，有延气的判断
        groupItems.add(circle);

        // 把这部分的棋子全部装进去
        visit(circle.getUp(), color, visited, groupItems);
        visit(circle.getDown(), color, visited, groupItems);
        visit(circle.getLeft(), color, visited, groupItems);
        visit(circle.getRight(), color, visited, groupItems);

        // 遍历栈中的所有棋子，判断气的个数，并将其中为空的棋子找出来(眼)
        Set<GoItem> breathSet = new HashSet<>();
        for (GoItem groupItem : groupItems) {
            if (groupItem.getUp() != null && groupItem.getUp().getColor() == null) {
                breathSet.add(groupItem.getUp());
            }
            if (groupItem.getDown() != null && groupItem.getDown().getColor() == null) {
                breathSet.add(groupItem.getDown());
            }
            if (groupItem.getLeft() != null && groupItem.getLeft().getColor() == null) {
                breathSet.add(groupItem.getLeft());
            }
            if (groupItem.getRight() != null && groupItem.getRight().getColor() == null) {
                breathSet.add(groupItem.getRight());
            }
        }
        return new GetBreathResult(breathSet.size(), groupItems);
    }

    private void visit(GoItem circle, Color color, boolean[][] visited, List<GoItem> groupItems) {
        // 棋子颜色不一致，或者已经访问过了，那么直接返回
        if (circle == null || circle.getColor() != color || visited[circle.getY()][circle.getX()]) {
            return;
        }
        // 将符合的棋子推入栈，并标记已访问
        groupItems.add(circle);
        visited[circle.getY()][circle.getX()] = true;

        visit(circle.getUp(), color, visited, groupItems);
        visit(circle.getDown(), color, visited, groupItems);
        visit(circle.getLeft(), color, visited, groupItems);
        visit(circle.getRight(), color, visited, groupItems);
    }

    public int currentStep() {
        return globalCurrentStep.get();
    }

    private int nextStep() {
        return globalCurrentStep.incrementAndGet();
    }

    private void rollbackStep(GoItem circle) {
        circle.setColor(null);
        globalCurrentStep.decrementAndGet();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
