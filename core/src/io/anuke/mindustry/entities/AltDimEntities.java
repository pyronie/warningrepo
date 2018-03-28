package io.anuke.mindustry.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.entities.SolidEntity;
import io.anuke.ucore.function.Consumer;
import io.anuke.ucore.function.Predicate;

public class AltDimEntities{
    public static final Object entityLock = new Object();

    private static final AltDimEntityGroup<AltDimEntity> defaultGroup;
    private static final Array<AltDimEntityGroup<?>> groupArray = new Array<>();
    private static final IntMap<AltDimEntityGroup<?>> groups = new IntMap<>();

    private static final AltDimEntityCollisions collisions = new AltDimEntityCollisions();
    private static final Array<SolidAltDimEntity> array = new Array<>();
    private static final Rectangle viewport = new Rectangle();
    private static final Rectangle r1 = new Rectangle();
    private static final Rectangle r2 = new Rectangle();

    public static final int maxLeafObjects = 4;

    static{
        defaultGroup = addGroup(AltDimEntity.class);
    }

    public static AltDimEntityCollisions collisions(){
        return collisions;
    }

    public static void initPhysics(float x, float y, float w, float h){
        for(AltDimEntityGroup group : groupArray){
            if(group.useTree)
                group.setTree(x, y, w, h);
        }
    }

    public static void initPhysics(){
        initPhysics(0, 0, 0, 0);
    }

    public static void resizeTree(float x, float y, float w, float h){
        initPhysics(x, y, w, h);
    }

    public static void getNearby(AltDimEntityGroup<?> group, Rectangle rect, Consumer<SolidAltDimEntity> out){
        synchronized (entityLock) {
            if (!group.useTree)
                throw new RuntimeException("This group does not support quadtrees! Enable quadtrees when creating it.");
            group.tree().getIntersect(out, rect);
        }
    }

    public static Array<SolidAltDimEntity> getNearby(AltDimEntityGroup<?> group, Rectangle rect){
        synchronized (entityLock) {
            array.clear();
            getNearby(group, rect, array::add);
            return array;
        }
    }

    public static void getNearby(float x, float y, float size, Consumer<SolidAltDimEntity> out){
        getNearby(defaultGroup(), r1.setSize(size).setCenter(x, y), out);
    }

    public static void getNearby(AltDimEntityGroup<?> group, float x, float y, float size, Consumer<SolidAltDimEntity> out){
        getNearby(group, r1.setSize(size).setCenter(x, y), out);
    }

    public static Array<SolidAltDimEntity> getNearby(float x, float y, float size){
        return getNearby(defaultGroup(), r1.setSize(size).setCenter(x, y));
    }

    public static Array<SolidAltDimEntity> getNearby(AltDimEntityGroup<?> group, float x, float y, float size){
        return getNearby(group, r1.setSize(size).setCenter(x, y));
    }

    public static SolidAltDimEntity getClosest(AltDimEntityGroup<?> group, float x, float y, float range, Predicate<AltDimEntity> pred){
        synchronized (entityLock) {
            SolidAltDimEntity closest = null;
            float cdist = 0f;
            Array<SolidAltDimEntity> entities = getNearby(group, x, y, range * 2f);
            for (int i = 0; i < entities.size; i++) {
                SolidAltDimEntity e = entities.get(i);
                if (!pred.test(e))
                    continue;

                float dist = Vector2.dst(e.x, e.y, x, y);
                if (dist < range)
                    if (closest == null || dist < cdist) {
                        closest = e;
                        cdist = dist;
                    }
            }

            return closest;
        }
    }

    public static void clear(){
        for(AltDimEntityGroup group : groupArray){
            group.clear();
        }
    }

    public static Iterable<AltDimEntity> all(){
        return defaultGroup.all();
    }

    public static AltDimEntityGroup<?> getGroup(int id){
        return groups.get(id);
    }

    public static Iterable<AltDimEntityGroup<?>> getAllGroups(){
        return groups.values();
    }

    public static AltDimEntityGroup<AltDimEntity> defaultGroup(){
        return defaultGroup;
    }

    public static <T extends AltDimEntity> AltDimEntityGroup<T> addGroup(Class<T> type){
        return addGroup(type, true);
    }

    public static <T extends AltDimEntity> AltDimEntityGroup<T> addGroup(Class<T> type, boolean useTree){
        AltDimEntityGroup<T> group = new AltDimEntityGroup<>(type, useTree);
        groups.put(group.getID(), group);
        groupArray.add(group);
        return group;
    }

    public static void collideGroups(AltDimEntityGroup<?> groupa, AltDimEntityGroup<?> groupb){
        collisions().collideGroups(groupa, groupb);
    }

    public static void draw(){
        draw(defaultGroup);
    }

    public static <T extends AltDimEntity> void draw(AltDimEntityGroup<T> group){
        draw(group, e -> true);
    }

    public static <T extends AltDimEntity> void draw(AltDimEntityGroup<T> group, Predicate<T> toDraw){
        OrthographicCamera cam = Core.camera;
        viewport.set(cam.position.x - cam.viewportWidth / 2 * cam.zoom, cam.position.y - cam.viewportHeight / 2 * cam.zoom, cam.viewportWidth * cam.zoom, cam.viewportHeight * cam.zoom);

        for(AltDimEntity e : group.all()){
            if(!toDraw.test((T)e)) continue;
            r2.setSize(e.drawSize()).setCenter(e.x, e.y);

            if(r2.overlaps(viewport))
                e.draw();
        }

        for(AltDimEntity e : group.all()){
            if(!toDraw.test((T)e)) continue;
            r2.setSize(e.drawSize()).setCenter(e.x, e.y);

            if(r2.overlaps(viewport))
                e.drawOver();
        }
    }

    public static void update(){
        update(defaultGroup());
        collideGroups(defaultGroup(), defaultGroup());
    }

    public static void update(AltDimEntityGroup<?> group){

        group.updateEvents();

        if(group.useTree){
            collisions().updatePhysics(group);
        }

        for(AltDimEntity e : group.all()){
            e.update();
        }
    }
}