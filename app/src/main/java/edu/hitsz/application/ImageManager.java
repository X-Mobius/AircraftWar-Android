package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;
import edu.hitsz.aircraft.*;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.property.*;

public class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();
    private static boolean initialized = false;

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap BACKGROUND_IMAGE_EASY;
    public static Bitmap BACKGROUND_IMAGE_MEDIUM;
    public static Bitmap BACKGROUND_IMAGE_HARD;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITE_PLUS_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap BLOOD_SUPPLY_IMAGE;
    public static Bitmap BULLET_SUPPLY_IMAGE;
    public static Bitmap BOMB_SUPPLY_IMAGE;
    public static Bitmap BULLET_PLUS_SUPPLY_IMAGE;

    public static void init(Context context) {
        if (initialized) return;

        BACKGROUND_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
        BACKGROUND_IMAGE_EASY = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg);
        BACKGROUND_IMAGE_MEDIUM = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg3);
        BACKGROUND_IMAGE_HARD = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg5);

        HERO_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.hero);
        MOB_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.mob);
        ELITE_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.elite);
        HERO_BULLET_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_hero);
        ENEMY_BULLET_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_enemy);
        ELITE_PLUS_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.eliteplus);
        BOSS_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.boss);
        BLOOD_SUPPLY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_blood);
        BULLET_SUPPLY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bullet);
        BOMB_SUPPLY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bomb);
        BULLET_PLUS_SUPPLY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bulletplus);

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITE_PLUS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProperty.class.getName(), BLOOD_SUPPLY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletProperty.class.getName(), BULLET_SUPPLY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProperty.class.getName(), BOMB_SUPPLY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BulletPlusProperty.class.getName(), BULLET_PLUS_SUPPLY_IMAGE);

        initialized = true;
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        return obj == null ? null : get(obj.getClass().getName());
    }
}
