package net.elidhan.anim_guns.entity.projectile;

import mod.azure.azurelib.core.utils.MathUtils;
import net.elidhan.anim_guns.AnimatedGuns;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BulletProjectileEntity extends PersistentProjectileEntity
{
    private int pelletGroupCount;
    private int maxLife;
    private int lifeTicks;
    private Vec3d vel;

    public BulletProjectileEntity(EntityType<? extends BulletProjectileEntity> entityEntityType, World world)
    {
        super(entityEntityType, world);
    }

    public BulletProjectileEntity(LivingEntity owner, World world, float bulletDamage, int pelletGroupCount)
    {
        super(AnimatedGuns.BulletEntityType, owner, world);
        this.maxLife = 10;
        this.lifeTicks = 0;
        this.pelletGroupCount = pelletGroupCount;
        this.setDamage(bulletDamage);
        this.setNoGravity(true);
    }

    @Override
    public void tick()
    {
        super.tick();

        this.setVelocity(vel);

        if(this.lifeTicks++ >= this.maxLife)
        {
            this.discard();
        }
    }

    @Override
    public boolean isCritical()
    {
        return false;
    }

    @Override
    public boolean isShotFromCrossbow()
    {
        return false;
    }

    @Override
    public boolean hasNoGravity()
    {
        return !this.isTouchingWater();
    }

    @Override
    protected ItemStack asItemStack()
    {
        return null;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult)
    {
        Entity entity = entityHitResult.getEntity();
        if(entity.damage(entity.getDamageSources().arrow(this, this.getOwner() != null ? this.getOwner() : this), (float)this.getDamage()))
        {
            entity.timeUntilRegen = 0;
            if(entity instanceof EnderDragonPart) ((EnderDragonPart) entity).owner.timeUntilRegen = 0;
        }
        this.discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult)
    {
        if (!this.getEntityWorld().isClient())
        {
            Block block = this.getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock();
            float volumeAdjust = this.pelletGroupCount > 1 ? MathUtils.clamp((1.0f/(float)this.pelletGroupCount), 0.2f, 1.0f) : 1.0f;
            this.getEntityWorld().playSound(null, blockHitResult.getBlockPos(), this.getEntityWorld().getBlockState(blockHitResult.getBlockPos()).getSoundGroup().getBreakSound(), SoundCategory.BLOCKS, volumeAdjust, 1.0f);
            
            if (!(block instanceof BarrierBlock))
                {
                ((ServerWorld)this.getEntityWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, this.getEntityWorld().getBlockState(blockHitResult.getBlockPos())), blockHitResult.getPos().getX(), blockHitResult.getPos().getY(), blockHitResult.getPos().getZ(), 5, 0.0, 0.0, 0.0, 0.5f);
                }
        }
        BlockState blockState = this.getWorld().getBlockState(blockHitResult.getBlockPos());
        blockState.onProjectileHit(this.getWorld(), blockState, blockHitResult, this);

        this.discard();
    }

    @Override
    protected SoundEvent getHitSound()
    {
        return SoundEvents.INTENTIONALLY_EMPTY;
    }

    public void setBaseVel(Vec3d velocity) {
        this.vel = velocity;
    }
}
