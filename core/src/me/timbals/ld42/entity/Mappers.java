package me.timbals.ld42.entity;

import com.badlogic.ashley.core.ComponentMapper;
import me.timbals.ld42.entity.components.*;

public class Mappers {

    // This class has a mapper for each entity component to quickly access components
    public static final ComponentMapper<PositionComponent> position = ComponentMapper.getFor(PositionComponent.class);
    public static final ComponentMapper<VelocityComponent> velocity = ComponentMapper.getFor(VelocityComponent.class);
    public static final ComponentMapper<ControlComponent> control = ComponentMapper.getFor(ControlComponent.class);
    public static final ComponentMapper<TextureComponent> texture = ComponentMapper.getFor(TextureComponent.class);
    public static final ComponentMapper<SizeComponent> size = ComponentMapper.getFor(SizeComponent.class);
    public static final ComponentMapper<RotationComponent> rotation = ComponentMapper.getFor(RotationComponent.class);
    public static final ComponentMapper<PlanetPositionComponent> planetPosition = ComponentMapper.getFor(PlanetPositionComponent.class);
    public static final ComponentMapper<CameraFollowComponent> cameraFollow = ComponentMapper.getFor(CameraFollowComponent.class);
    public static final ComponentMapper<SmashToRocksComponent> smashToRocks = ComponentMapper.getFor(SmashToRocksComponent.class);
    public static final ComponentMapper<GravityComponent> gravity = ComponentMapper.getFor(GravityComponent.class);
    public static final ComponentMapper<InertiaComponent> inertia = ComponentMapper.getFor(InertiaComponent.class);
    public static final ComponentMapper<CollectibleComponent> collectible = ComponentMapper.getFor(CollectibleComponent.class);
    public static final ComponentMapper<CollectComponent> collect = ComponentMapper.getFor(CollectComponent.class);
    public static final ComponentMapper<AnimationComponent> animation = ComponentMapper.getFor(AnimationComponent.class);
    public static final ComponentMapper<VisibilityComponent> visibility = ComponentMapper.getFor(VisibilityComponent.class);
    public static final ComponentMapper<TagComponent> tag = ComponentMapper.getFor(TagComponent.class);
    public static final ComponentMapper<TargetComponent> target = ComponentMapper.getFor(TargetComponent.class);

}