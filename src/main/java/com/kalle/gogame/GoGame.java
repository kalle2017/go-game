package com.kalle.gogame;

import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.*;
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

    private static final GoColor[] GO_STATUS_MACHINE = {
            GoColor.WHITE,
            GoColor.BLACK
    };
    private static final Color[] COLOR_STATUS_MACHINE = {
            Color.WHITE,
            Color.BLACK
    };

    private static final Color[] HOVER_COLOR_STATUS_MACHINE = {
            Color.rgb(255, 255, 255, 0.3),
            Color.rgb(0, 0, 0, 0.3),
    };

    private GoItem[][] goItems;

    // 当前步数
    private final AtomicInteger globalCurrentStep = new AtomicInteger(0);

    @Override
    public void start(Stage stage) {
        Group box = new Group();

        Scene scene = new Scene(box, 800, 640);
        scene.setFill(Color.rgb(216, 178, 133));

        int road = 19, dest = 30;
        int x0 = 50, y0 = 50;
        int maxDest = (road - 1) * dest;

        // 画横竖线
        // 生成按钮

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

            // TODO 围棋上的小圆点
        }

        // 初始化棋盘
        goItems = new GoItem[road][road];

        for (int y = 0; y < road; y++) {
            for (int x = 0; x < road; x++) {
                GoItem circle = new GoItem(x, y, road);
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

                circle.setCursor(Cursor.HAND);
                circle.setOnMouseMoved(e -> {
                    if (circle.getColor() == null) {
                        int currentStep = currentStep();
                        int status = (currentStep + 1) % 2;
                        circle.setFill(HOVER_COLOR_STATUS_MACHINE[status]);
                    }
                });
                circle.setOnMouseExited(e -> {
                    if (circle.getColor() == null) {
                        circle.setFill(Color.TRANSPARENT);
                    }
                });

                // 点击事件
                circle.setOnMouseClicked(e -> {

                    // 播放音效
                    wavPlayer.play();

                    if (circle.getColor() == null) {
                        int nextStep = nextStep();
                        int status = nextStep % 2;

                        GoColor currentColor = GO_STATUS_MACHINE[status];
                        circle.setColor(currentColor);

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
                            // 判断是否能杀棋，暂时不判断打劫的情况

                            // 不入气
                            if (killGoItems.isEmpty()) {
                                System.out.println("当前位置不入气");
                                rollbackStep(circle);
                                return;
                            }
                        }

                        for (GoItem killGoItem : killGoItems) {
                            killGoItem.setColor(null);
                            killGoItem.setFill(Color.TRANSPARENT);
                        }

                        wavPlayer.remove(killGoItems.size());

                        // 设置棋子颜色
                        circle.setFill(COLOR_STATUS_MACHINE[status]);
                    }
                });

                box.getChildren().add(circle);
            }
        }

        // 增加重新开始按钮
        Button restartButton = new Button("重新开始");
        restartButton.setPrefSize(100, 30);
        restartButton.setBorder(Border.EMPTY);
        restartButton.setLayoutX(650);
        restartButton.setLayoutY(60);

        restartButton.setOnMouseClicked(e -> reset());

        box.getChildren().add(restartButton);

        stage.setScene(scene);
        stage.show();
    }

    public void reset() {
        globalCurrentStep.set(0);

        for (GoItem[] goItemRow : goItems) {
            for (GoItem goItem : goItemRow) {
                goItem.setColor(null);
                goItem.setFill(Color.TRANSPARENT);
            }
        }
    }

    private Set<GoItem> findKillGoItems(GoColor goColor, GoItem goItem) {
        Set<GoItem> killGoItems = new HashSet<>();
        if (goItem != null && goItem.getColor() != null && goItem.getColor() != goColor) {
            GetBreathResult breathResult = getBreaths(goItem);
            if (breathResult.getBreaths() == 0) {
                killGoItems.addAll(breathResult.getGroupItems());
            }
        }
        return killGoItems;
    }

    private GetBreathResult getBreaths(GoItem circle) {
        GoColor goColor = circle.getColor();

        boolean[][] visited = new boolean[goItems.length][goItems.length];
        List<GoItem> groupItems = new ArrayList<>();

        // 需要加入当前棋子，有延气的判断
        groupItems.add(circle);

        // 把这部分的棋子全部装进去
        visit(circle.getUp(), goColor, visited, groupItems);
        visit(circle.getDown(), goColor, visited, groupItems);
        visit(circle.getLeft(), goColor, visited, groupItems);
        visit(circle.getRight(), goColor, visited, groupItems);

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

    private void visit(GoItem circle, GoColor color, boolean[][] visited, List<GoItem> groupItems) {
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

    private int currentStep() {
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
