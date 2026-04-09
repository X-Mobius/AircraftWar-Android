package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.property.BaseProperty;
import edu.hitsz.property.BloodFactory;
import edu.hitsz.property.BombFactory;
import edu.hitsz.property.BombProperty;
import edu.hitsz.property.BulletFactory;
import edu.hitsz.property.BulletPlusFactory;
import edu.hitsz.property.PropertyFactory;
import edu.hitsz.sound.SoundManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 游戏主流程抽象类。
 * 负责通用的游戏循环、绘制、触摸输入和碰撞处理，
 * 难度相关差异由子类实现。
 */
public abstract class AbstractGame extends SurfaceView
        implements SurfaceHolder.Callback, Runnable, View.OnTouchListener {

    public static final int MSG_GAME_OVER = 1001;

    protected Bitmap backgroundImage;
    protected int backGroundTop = 0;

    /** 线程池，当前主要用于兼容旧结构。 */
    protected final ScheduledExecutorService executorService;

    /** 帧间隔（毫秒）。 */
    protected int timeInterval = 40;

    protected final HeroAircraft heroAircraft;
    protected final List<AbstractAircraft> enemyAircrafts;
    protected final List<BaseBullet> heroBullets;
    protected final List<BaseBullet> enemyBullets;
    protected final List<BaseProperty> properties;

    /** 场上敌机最大数量。 */
    protected int enemyMaxNumber;

    /** 当前得分。 */
    protected int score = 0;

    /** 上一次生成 Boss 时对应的分数阈值。 */
    protected int lastBossScore = 0;

    /** 游戏运行时间。 */
    protected int time = 0;

    /** 周期参数，用于控制生成和射击频率。 */
    protected int cycleDuration = 600;
    protected int cycleTime = 0;

    /** 游戏结束标志。 */
    protected boolean gameOverFlag = false;

    /** 逻辑坐标尺寸（暂与旧版保持一致）。 */
    protected int logicalWidth = 512;
    protected int logicalHeight = 768;

    protected int surfaceWidth = logicalWidth;
    protected int surfaceHeight = logicalHeight;

    protected SurfaceHolder surfaceHolder;
    protected volatile boolean isDrawing = false;
    protected Thread drawThread;
    protected Paint textPaint;
    protected float renderScaleX = 1f;
    protected float renderScaleY = 1f;
    protected float renderOffsetX = 0f;
    protected float renderOffsetY = 0f;
    protected boolean heroPositionInitialized = false;
    private Handler uiHandler;
    private boolean gameOverMessageSent = false;

    public AbstractGame() {
        this(resolveContext());
    }

    public AbstractGame(Context context) {
        super(context);

        heroAircraft = HeroAircraft.getHeroAircraft();
        int defaultX = Main.WINDOW_WIDTH / 2;
        int heroImageHeight = ImageManager.HERO_IMAGE == null ? 0 : ImageManager.HERO_IMAGE.getHeight();
        int defaultY = Main.WINDOW_HEIGHT - heroImageHeight;
        heroAircraft.resetForNewGame(defaultX, defaultY);
        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        properties = new LinkedList<>();

        executorService = new ScheduledThreadPoolExecutor(1);

        initSurfaceComponents();
    }

    private static Context resolveContext() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object application = activityThread.getMethod("currentApplication").invoke(null);
            if (application instanceof Context) {
                return (Context) application;
            }
        } catch (Exception ignored) {
            // no-op
        }
        throw new IllegalStateException("Context is required for AbstractGame SurfaceView.");
    }

    private void initSurfaceComponents() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setOnTouchListener(this);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(44f);
    }

    /** 启动游戏循环（兼容旧调用方式）。 */
    public void action() {
        startDrawThreadIfNeeded();
    }

    public void setUiHandler(Handler handler) {
        this.uiHandler = handler;
    }

    private synchronized void startDrawThreadIfNeeded() {
        if (drawThread != null && drawThread.isAlive()) {
            return;
        }
        isDrawing = true;
        drawThread = new Thread(this, "game-surface-loop");
        drawThread.start();
    }

    private synchronized void stopDrawThread() {
        isDrawing = false;
        if (drawThread != null) {
            drawThread.interrupt();
            try {
                drawThread.join(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            drawThread = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        SoundManager.playBgm();
        startDrawThreadIfNeeded();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceWidth = Math.max(1, width);
        surfaceHeight = Math.max(1, height);
        logicalWidth = surfaceWidth;
        logicalHeight = surfaceHeight;
        Main.updateWindowSize(logicalWidth, logicalHeight);
        updateRenderTransform(surfaceWidth, surfaceHeight);

        if (ImageManager.HERO_IMAGE != null) {
            int defaultX = logicalWidth / 2;
            int defaultY = logicalHeight - ImageManager.HERO_IMAGE.getHeight();
            if (!heroPositionInitialized
                    || heroAircraft.getLocationX() < 0
                    || heroAircraft.getLocationX() > logicalWidth
                    || heroAircraft.getLocationY() < 0
                    || heroAircraft.getLocationY() > logicalHeight) {
                heroAircraft.setLocation(defaultX, defaultY);
                heroPositionInitialized = true;
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        SoundManager.stopBgm();
        stopDrawThread();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (measuredWidth <= 0) {
            measuredWidth = logicalWidth;
        }
        if (measuredHeight <= 0) {
            measuredHeight = logicalHeight;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public void run() {
        while (isDrawing) {
            long frameStart = System.currentTimeMillis();

            updateGameFrame();

            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    drawFrame(canvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            long frameCost = System.currentTimeMillis() - frameStart;
            long sleep = timeInterval - frameCost;
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
            return false;
        }
        if (gameOverFlag) {
            return true;
        }

        // Use real view size first to avoid stale mapping before/without surfaceChanged callback.
        int vw = Math.max(1, view.getWidth());
        int vh = Math.max(1, view.getHeight());
        if (vw <= 1 || vh <= 1) {
            vw = Math.max(1, surfaceWidth);
            vh = Math.max(1, surfaceHeight);
        }
        updateRenderTransform(vw, vh);

        int x = (int) ((motionEvent.getX() - renderOffsetX) / renderScaleX);
        int y = (int) ((motionEvent.getY() - renderOffsetY) / renderScaleY);

        int heroHalfW = ImageManager.HERO_IMAGE == null ? 0 : ImageManager.HERO_IMAGE.getWidth() / 2;
        int heroHalfH = ImageManager.HERO_IMAGE == null ? 0 : ImageManager.HERO_IMAGE.getHeight() / 2;

        // Keep hero fully inside logical game area.
        int minX = heroHalfW;
        int maxX = logicalWidth - heroHalfW;
        int minY = heroHalfH;
        int maxY = logicalHeight - heroHalfH;

        x = Math.max(minX, Math.min(maxX, x));
        y = Math.max(minY, Math.min(maxY, y));
        heroAircraft.setLocation(x, y);
        return true;
    }

    private void updateGameFrame() {
        time += timeInterval;

        // 按周期执行生成、难度更新和射击
        if (timeCountAndNewCycleJudge()) {
            System.out.println(time);
            difficultyUpdate(time);
            generateEnemy();
            shootAction();
        }

        // 通用更新流程
        bulletsMoveAction();
        propsMoveAction();
        aircraftsMoveAction();
        crashCheckAction();
        postProcessAction();

        // 结束判定
        if (!gameOverFlag && heroAircraft.getHp() <= 0) {
            gameOverFlag = true;
            isDrawing = false;
            executorService.shutdown();
            System.out.println("Game Over!");
            dispatchGameOverMessage();
            onGameOver();
        }
    }

    // ================= 模板钩子（由子类实现） =================

    /** 难度随时间变化。 */
    private void dispatchGameOverMessage() {
        if (gameOverMessageSent || uiHandler == null) {
            return;
        }
        gameOverMessageSent = true;
        Message message = uiHandler.obtainMessage(MSG_GAME_OVER);
        message.arg1 = score;
        uiHandler.sendMessage(message);
    }

    protected abstract void difficultyUpdate(int time);

    /** 敌机生成策略。 */
    protected abstract void generateEnemy();

    /** 游戏结束时的处理。 */
    protected abstract void onGameOver();

    // ================= 以下是各难度通用逻辑 =================

    protected boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    protected void shootAction() {
        // 敌机射击
        for (AbstractAircraft enemy : enemyAircrafts) {
            if (enemy instanceof EliteEnemy || enemy instanceof ElitePlusEnemy || enemy instanceof BossEnemy) {
                enemyBullets.addAll(enemy.shoot());
            }
        }

        // 英雄机射击
        heroBullets.addAll(heroAircraft.shoot());
    }

    protected void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    protected void propsMoveAction() {
        for (BaseProperty property : properties) {
            property.forward();
        }
    }

    protected void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
    }

    /** 道具随机生成。 */
    protected BaseProperty randomProperty(AbstractAircraft enemy) {
        double random = Math.random();
        PropertyFactory factory;

        if (random < 0.33) {
            factory = new BloodFactory();
        } else if (random < 0.66) {
            factory = new BulletFactory();
        } else {
            factory = new BombFactory();
        }

        return factory.createProperty(
                enemy.getLocationX(),
                enemy.getLocationY(),
                0, 5
        );
    }

    /**
     * 碰撞检测：
     * 1. 敌机子弹打中英雄机
     * 2. 英雄机子弹打中敌机
     * 3. 英雄机获取道具
     */
    protected void crashCheckAction() {
        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已失效敌机不再判定，避免重复击毁
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    SoundManager.playSoundEffect("src/videos/bullet_hit.wav");
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // 加分
                        if (enemyAircraft instanceof BossEnemy) {
                            SoundManager.playBgm();
                            score += 75;
                        } else if (enemyAircraft instanceof ElitePlusEnemy) {
                            score += 30;
                        } else if (enemyAircraft instanceof EliteEnemy) {
                            score += 20;
                        } else {
                            score += 10;
                        }

                        // 掉落道具
                        if (enemyAircraft instanceof EliteEnemy
                                || enemyAircraft instanceof ElitePlusEnemy
                                || enemyAircraft instanceof BossEnemy) {

                            double dropChance = Math.random();
                            double threshold;
                            if (enemyAircraft instanceof BossEnemy) {
                                threshold = 1.0; // Boss 必掉
                            } else if (enemyAircraft instanceof ElitePlusEnemy) {
                                threshold = 0.95;
                            } else {
                                threshold = 0.9;
                            }

                            if (dropChance < threshold) {
                                int dropCount = 1;
                                if (enemyAircraft instanceof BossEnemy) {
                                    dropCount = (int) (Math.random() * 3) + 1;
                                }

                                for (int i = 0; i < dropCount; i++) {
                                    double random = Math.random();
                                    PropertyFactory factory;

                                    if (random < 0.3) {
                                        factory = new BloodFactory();
                                    } else if (random < 0.6) {
                                        factory = new BombFactory();
                                    } else if (random < 0.8) {
                                        factory = new BulletFactory();
                                    } else {
                                        factory = new BulletPlusFactory();
                                    }

                                    // 多个道具时横向错开一点
                                    int offsetX = (dropCount > 1) ? (i - dropCount / 2) * 20 : 0;

                                    BaseProperty property = factory.createProperty(
                                            enemyAircraft.getLocationX() + offsetX,
                                            enemyAircraft.getLocationY(),
                                            0,
                                            5
                                    );
                                    properties.add(property);
                                }
                            }
                        }
                    }
                }

                // 英雄机与敌机相撞
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // 英雄机拾取道具
        for (BaseProperty property : properties) {
            if (property.notValid()) {
                continue;
            }
            if (heroAircraft.crash(property)) {
                property.activate(heroAircraft);
                if (property instanceof BombProperty) {
                    ((BombProperty) property).activate(heroAircraft, enemyAircrafts, enemyBullets, this);
                }
                property.vanish();
            }
        }
    }

    /**
     * 后处理：
     * 1. 删除无效子弹
     * 2. 删除无效敌机
     * 3. 删除无效道具
     */
    protected void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        properties.removeIf(AbstractFlyingObject::notValid);
    }

    /** 分数处理。 */
    public void addScore(int delta) {
        this.score += delta;
    }

    public int getScore() {
        return this.score;
    }

    private void drawFrame(Canvas canvas) {
        if (canvas == null) {
            return;
        }

        canvas.drawColor(Color.BLACK);
        updateRenderTransform(canvas.getWidth(), canvas.getHeight());
        canvas.save();
        canvas.translate(renderOffsetX, renderOffsetY);
        canvas.scale(renderScaleX, renderScaleY);

        if (backgroundImage != null) {
            // 绘制滚动背景
            Rect dstTop = new Rect(0, this.backGroundTop - logicalHeight, logicalWidth, this.backGroundTop);
            Rect dstBottom = new Rect(0, this.backGroundTop, logicalWidth, this.backGroundTop + logicalHeight);
            canvas.drawBitmap(backgroundImage, null, dstTop, null);
            canvas.drawBitmap(backgroundImage, null, dstBottom, null);
            this.backGroundTop += 1;
            if (this.backGroundTop >= logicalHeight) {
                this.backGroundTop = 0;
            }
        }

        // 先子弹后飞机，保证层级关系
        paintImageWithPositionRevised(canvas, enemyBullets);
        paintImageWithPositionRevised(canvas, heroBullets);
        paintImageWithPositionRevised(canvas, enemyAircrafts);
        paintImageWithPositionRevised(canvas, properties);

        if (ImageManager.HERO_IMAGE != null) {
            canvas.drawBitmap(ImageManager.HERO_IMAGE,
                    heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2f,
                    heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2f,
                    null);
        }

        paintScoreAndLife(canvas);
        canvas.restore();
    }

    protected void updateRenderTransform(int width, int height) {
        int safeW = Math.max(1, width);
        int safeH = Math.max(1, height);
        float sx = safeW / (float) logicalWidth;
        float sy = safeH / (float) logicalHeight;
        renderScaleX = sx > 0f ? sx : 1f;
        renderScaleY = sy > 0f ? sy : 1f;
        renderOffsetX = 0f;
        renderOffsetY = 0f;
    }

    private void paintImageWithPositionRevised(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            Bitmap image = object.getImage();
            if (image == null) {
                continue;
            }
            canvas.drawBitmap(image,
                    object.getLocationX() - image.getWidth() / 2f,
                    object.getLocationY() - image.getHeight() / 2f,
                    null);
        }
    }

    private void paintScoreAndLife(Canvas canvas) {
        float x = 10f;
        float y = 45f;
        canvas.drawText("SCORE:" + this.score, x, y, textPaint);
        y += 40f;
        canvas.drawText("LIFE:" + this.heroAircraft.getHp(), x, y, textPaint);
    }
}
