package com.kalle.gogame;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 围棋游戏
 *
 * @author kalle
 * @since 2022-11-18
 */
public class GoGame extends Application {

    /**
     * 围棋路数
     */
    private static final int ROAD = 19;

    /**
     * 边距
     */
    private static final int PADDING = 30;

    /**
     * 容器左边距
     */
    private static final int X0 = 50;

    /**
     * 容器上边距
     */
    private static final int Y0 = 50;

    /**
     * 容器:填充所有元素
     */
    private static final Group CONTAINER = new Group();

    /**
     * 棋子颜色状态机
     */
    private static final Color[] COLOR_STATUS_MACHINE = {
            Color.WHITE,
            Color.BLACK
    };

    /**
     * 棋盘棋子数组
     */
    private GoItem[][] goItems;

    /**
     * 当前步数
     */
    private final AtomicInteger globalCurrentStep = new AtomicInteger(0);

    /**
     * 行棋记录
     */
    private final Stack<GoRecord> doLog = new Stack<>();

    /**
     * 回退记录
     */
    private final Stack<GoRecord> undoLog = new Stack<>();

    /**
     * 当前指针，指在需要当前棋子上
     */
    private static final Polygon POINTER = new Polygon(0, 0, 11, 0, 0, 11);

    @Override
    public void start(Stage stage) {

        // 设置关闭事件
        stage.setOnCloseRequest(e -> System.exit(0));

        Scene scene = new Scene(CONTAINER, 800, 640);
        scene.setFill(Color.rgb(216, 178, 133));
        stage.setResizable(false);

        // 绘制棋盘
        this.drawGoTable();

        // 初始化棋盘
        this.initGoItems();

        // 增加操作按钮
        this.addOperationListener();

        // 显示窗口
        stage.setTitle("围棋游戏 v1.0");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 添加操作按钮
     */
    private void addOperationListener() {
        // 增加重新开始按钮
        Button restartButton = new Button("重新开始");
        restartButton.setPrefSize(100, 30);
        restartButton.setBorder(Border.EMPTY);
        restartButton.setLayoutX(650);
        restartButton.setLayoutY(60);

        restartButton.setOnMouseClicked(e -> reset());

        // 增加后退按钮
        Button backwardButton = new Button("后退");
        backwardButton.setPrefSize(100, 30);
        backwardButton.setBorder(Border.EMPTY);
        backwardButton.setLayoutX(650);
        backwardButton.setLayoutY(120);

        backwardButton.setOnMouseClicked(e -> backward());

        // 增加前进按钮
        Button forwardButton = new Button("前进");
        forwardButton.setPrefSize(100, 30);
        forwardButton.setBorder(Border.EMPTY);
        forwardButton.setLayoutX(650);
        forwardButton.setLayoutY(180);

        forwardButton.setOnMouseClicked(e -> forward());

        // 添加到容器中
        GoGame.CONTAINER.getChildren().add(restartButton);
        GoGame.CONTAINER.getChildren().add(backwardButton);
        GoGame.CONTAINER.getChildren().add(forwardButton);
    }

    /**
     * 初始化棋盘
     */
    private void initGoItems() {
        // 初始化棋盘
        goItems = new GoItem[GoGame.ROAD][GoGame.ROAD];

        for (int y = 0; y < GoGame.ROAD; y++) {
            for (int x = 0; x < GoGame.ROAD; x++) {
                GoItem current = new GoItem(x, y, this);
                goItems[y][x] = current;

                current.setRadius(13);
                current.setCenterX(GoGame.X0 + x * GoGame.PADDING);
                current.setCenterY(GoGame.Y0 + y * GoGame.PADDING);
                current.setFill(Color.TRANSPARENT);

                // 上下
                if (y > 0) {
                    current.setUp(goItems[y - 1][x]);
                    // 下自己设置不了，需要上一个去设置
                    goItems[y - 1][x].setDown(current);
                }
                // 左右
                if (x > 0) {
                    current.setLeft(goItems[y][x - 1]);
                    goItems[y][x - 1].setRight(current);
                }

                // 点击事件
                current.setOnMouseClicked(e -> put((GoItem) e.getSource()));

                CONTAINER.getChildren().add(current);
            }
        }
    }

    /**
     * 落子监听事件
     *
     * @param current current
     */
    private void put(GoItem current) {
        // 落子音效
        WavPlayer.play();

        if (current.getColor() == null) {
            // 步数增加
            int nextStep = globalCurrentStep.incrementAndGet();
            int status = nextStep % 2;

            // 设置棋子颜色，目的是后面判断杀气的时候，判断气的数量
            current.setColor(COLOR_STATUS_MACHINE[status]);

            // 吃子判断
            // 遍历上下左右的棋子，判断是否
            // 判断是否不入气，从当前棋子出发，是否没有气
            GetBreathResult result = getBreaths(current);

            // 判断当前棋子的上下左右，是否存在不同颜色的棋子
            Set<GoItem> killGoItems = new HashSet<>();
            List<GoItem> directionGoItems = Arrays.asList(current.getUp(), current.getDown(), current.getLeft(), current.getRight());
            for (GoItem directionGoItem : directionGoItems) {
                killGoItems.addAll(findKillGoItems(current.getColor(), directionGoItem));
            }

            if (result.getBreaths() == 0) {
                // 不入气
                if (killGoItems.isEmpty()) {
                    Toast.toast("不入气");
                    rollbackStep(current);
                    return;
                }

                // 打劫的情况
                if (!doLog.isEmpty()) {
                    GoRecord peek = doLog.peek();
                    // 上一手刚好只吃掉一个子，而且是自己，则打劫
                    if (peek.getKillGoItems().size() == 1 && peek.getKillGoItems().contains(current)) {
                        Toast.toast("打劫");
                        rollbackStep(current);
                        return;
                    }
                }
            }

            for (GoItem killGoItem : killGoItems) {
                killGoItem.setColor(null);
            }

            // 吃子音效
            WavPlayer.remove(killGoItems.size());

            // 每成功走一步，那么就将当前结果压入历史栈
            GoRecord record = new GoRecord(current.getColor(), current, killGoItems);
            doLog.push(record);

            // 走一步棋，清空undoLog
            undoLog.clear();

            // 更新指针位置
            updatePointer(current);
        }
    }

    /**
     * 绘制棋盘
     */
    private void drawGoTable() {
        // 画横竖线
        // 生成按钮
        int maxDest = (GoGame.ROAD - 1) * GoGame.PADDING;

        for (int i = 0; i < GoGame.ROAD; i++) {
            // 横线
            Line lineH = new Line();

            lineH.setStartX(GoGame.X0);
            lineH.setStartY(GoGame.Y0 + i * GoGame.PADDING);
            lineH.setEndX(GoGame.X0 + maxDest);
            lineH.setEndY(GoGame.Y0 + i * GoGame.PADDING);

            // 竖线
            Line lineV = new Line();

            lineV.setStartX(GoGame.X0 + i * GoGame.PADDING);
            lineV.setStartY(GoGame.Y0);
            lineV.setEndX(GoGame.X0 + i * GoGame.PADDING);
            lineV.setEndY(GoGame.Y0 + maxDest);

            CONTAINER.getChildren().add(lineH);
            CONTAINER.getChildren().add(lineV);
        }

        // 围棋上的小圆点
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Circle circle = new Circle();
                circle.setRadius(4);
                circle.setCenterX(GoGame.X0 + 3 * GoGame.PADDING + j * 6 * GoGame.PADDING);
                circle.setCenterY(GoGame.Y0 + 3 * GoGame.PADDING + i * 6 * GoGame.PADDING);
                CONTAINER.getChildren().add(circle);
            }
        }
    }

    /**
     * 针对回退记录，前进一步，落子后会清空回退记录
     */
    public void forward() {
        // 落子音效
        WavPlayer.play();

        if (undoLog.isEmpty()) {
            Toast.toast("没有历史记录");
            return;
        }

        // 当前步数加一
        globalCurrentStep.incrementAndGet();

        // 将回退记录出栈
        GoRecord pop = undoLog.pop();

        // 前进一步
        pop.getKiller().setColor(pop.getColor());
        for (GoItem goItem : pop.getKillGoItems()) {
            goItem.setColor(null);
        }

        // 吃子音效
        WavPlayer.remove(pop.getKillGoItems().size());

        // 推入操作记录中
        doLog.push(pop);

        // 更新指针位置
        updatePointer(pop.getKiller());
    }

    /**
     * 针对操作记录，回退一步
     */
    public void backward() {
        // 落子音效
        WavPlayer.play();

        if (doLog.isEmpty()) {
            Toast.toast("没有历史记录");
            return;
        }

        // 当前步数减一
        globalCurrentStep.decrementAndGet();

        // 将操作记录出栈
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

        // 推入回退记录中
        undoLog.push(pop);

        // 更新指针位置
        updatePointer(doLog.peek().getKiller());
    }

    /**
     * 更新指针位置
     *
     * @param current current
     */
    public void updatePointer(GoItem current) {
        Color reverseColor = getReverseColor(current.getColor());

        POINTER.setLayoutX(X0 + current.getX() * PADDING);
        POINTER.setLayoutY(Y0 + current.getY() * PADDING);
        POINTER.setFill(reverseColor);

        showPointer();
    }

    /**
     * 显示指针
     */
    public void showPointer() {
        // 移除指针
        CONTAINER.getChildren().remove(POINTER);
        POINTER.setOpacity(1);

        // 添加指针
        CONTAINER.getChildren().add(POINTER);
    }

    /**
     * 隐藏指针
     */
    public void hidePointer() {
        // 移除指针
        CONTAINER.getChildren().remove(POINTER);

        POINTER.setOpacity(0);
    }

    /**
     * 获取当前颜色的反色，黑->白，白->黑，其余则透明
     *
     * @param color color
     * @return Color
     */
    private Color getReverseColor(Color color) {
        if (color == Color.BLACK) {
            return Color.WHITE;
        } else if (color == Color.WHITE) {
            return Color.BLACK;
        }

        // 找不到时，实际上为透明
        return Color.TRANSPARENT;
    }

    /**
     * 重置棋局
     */
    public void reset() {
        globalCurrentStep.set(0);

        // 隐藏指针
        hidePointer();

        // 消除棋盘上棋子的颜色
        for (GoItem[] goItemRow : goItems) {
            for (GoItem goItem : goItemRow) {
                if (goItem.getColor() != null) {
                    goItem.setColor(null);
                }
            }
        }
    }

    /**
     * 寻找杀棋集合
     *
     * @param color  color
     * @param goItem goItem
     * @return Set<GoItem>
     */
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

    /**
     * 获取棋子的气数
     *
     * @param goItem goItem
     * @return GetBreathResult
     */
    private GetBreathResult getBreaths(GoItem goItem) {
        Color color = goItem.getColor();

        boolean[][] visited = new boolean[goItems.length][goItems.length];
        List<GoItem> groupItems = new ArrayList<>();

        // 需要算入当前棋子，解决相连时的气数计算
        groupItems.add(goItem);

        // 访问棋子的上下左右四个方向相同的棋子，找到所有集合
        visit(goItem, color, visited, groupItems);

        // 遍历栈中的所有棋子，判断气的个数，算上眼位
        Set<GoItem> breathSet = new HashSet<>();
        for (GoItem groupItem : groupItems) {
            // 计算棋子的气
            checkAndAddBreathSet(breathSet, groupItem);
        }
        return new GetBreathResult(breathSet.size(), groupItems);
    }

    /**
     * 计算棋子的气
     *
     * @param breathSet breathSet
     * @param goItem    goItem
     */
    private void checkAndAddBreathSet(Set<GoItem> breathSet, GoItem goItem) {
        // 检查同色棋子是否存在气
        if (goItem.getUp() != null && goItem.getUp().getColor() == null) {
            breathSet.add(goItem.getUp());
        }
        // 检查同色下方棋子是否存在气
        if (goItem.getDown() != null && goItem.getDown().getColor() == null) {
            breathSet.add(goItem.getDown());
        }
        // 检查同色左方棋子是否存在气
        if (goItem.getLeft() != null && goItem.getLeft().getColor() == null) {
            breathSet.add(goItem.getLeft());
        }
        // 检查同色右方棋子是否存在气
        if (goItem.getRight() != null && goItem.getRight().getColor() == null) {
            breathSet.add(goItem.getRight());
        }
    }

    /**
     * 访问棋子的上下左右四个方向相同的棋子，找到所有集合
     *
     * @param goItem     goItem
     * @param color      color
     * @param visited    visited
     * @param groupItems groupItems
     */
    private void visit(GoItem goItem, Color color, boolean[][] visited, List<GoItem> groupItems) {
        // 棋子颜色不一致，或者已经访问过了，那么直接返回
        if (goItem == null || goItem.getColor() != color || visited[goItem.getY()][goItem.getX()]) {
            return;
        }
        // 将符合的棋子推入栈，并标记已访问
        groupItems.add(goItem);
        visited[goItem.getY()][goItem.getX()] = true;

        visit(goItem.getUp(), color, visited, groupItems);
        visit(goItem.getDown(), color, visited, groupItems);
        visit(goItem.getLeft(), color, visited, groupItems);
        visit(goItem.getRight(), color, visited, groupItems);
    }

    /**
     * 当前步数
     *
     * @return int
     */
    public int currentStep() {
        return globalCurrentStep.get();
    }

    /**
     * 回退棋子，用于不入气或者打劫情况
     *
     * @param goItem goItem
     */
    private void rollbackStep(GoItem goItem) {
        goItem.setColor(null);
        globalCurrentStep.decrementAndGet();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
