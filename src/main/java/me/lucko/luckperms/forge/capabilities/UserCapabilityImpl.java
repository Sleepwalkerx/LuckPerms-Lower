/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.forge.capabilities;

import me.lucko.luckperms.common.cacheddata.type.PermissionCache;
import me.lucko.luckperms.common.context.manager.QueryOptionsCache;
import me.lucko.luckperms.common.locale.TranslationManager;
import me.lucko.luckperms.common.model.User;
import me.lucko.luckperms.common.verbose.event.CheckOrigin;
import me.lucko.luckperms.forge.context.ForgeContextManager;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class UserCapabilityImpl implements UserCapability {

    @CapabilityInject(UserCapability.class)
    public static Capability<UserCapability> INSTANCE = null;

    public static void register(){
        CapabilityManager.INSTANCE.register(UserCapability.class, new CapStorage(), UserCapabilityImpl::new);
    }

    /*private static LazyOptional<UserCapability> getCapability(PlayerEntity player) {
        return player.getCapability();
        if (player.isRemoved()) {
            return player.getCapability(CAPABILITY);
        } else {
            player.reviveCaps();
            try {
                return player.getCapability(CAPABILITY);
            } finally {
                player.invalidateCaps();
            }
        }
    }*/


    /**
     * Gets a {@link UserCapability} for a given {@link ServerPlayerEntity}.
     *
     * @param player the player
     * @return the capability
     */
    public static @NotNull UserCapabilityImpl get(@NotNull PlayerEntity player) {
        return (UserCapabilityImpl) player.getCapability(INSTANCE).orElseThrow(() -> new IllegalStateException("Capability missing for " + player.getUUID()));
    }

    /**
     * Gets a {@link UserCapability} for a given {@link ServerPlayerEntity}.
     *
     * @param player the player
     * @return the capability, or null
     */
    public static @Nullable UserCapabilityImpl getNullable(@NotNull PlayerEntity player) {
        return (UserCapabilityImpl) player.getCapability(INSTANCE).resolve().orElse(null);
    }

    private final LazyOptional<UserCapability> handle = LazyOptional.of(() -> this);

    private boolean initialised = false;

    private User user;
    private QueryOptionsCache<ServerPlayerEntity> queryOptionsCache;
    private String language;
    private Locale locale;

    public UserCapabilityImpl() { }

    public void initialise(UserCapabilityImpl previous) {
        this.user = previous.user;
        this.queryOptionsCache = previous.queryOptionsCache;
        this.language = previous.language;
        this.locale = previous.locale;
        this.initialised = true;
    }

    public void initialise(User user, ServerPlayerEntity player, ForgeContextManager contextManager) {
        this.user = user;
        this.queryOptionsCache = new QueryOptionsCache<>(player, contextManager);
        this.initialised = true;
    }

    private void assertInitialised() {
        if (!this.initialised) {
            throw new IllegalStateException("Capability has not been initialised");
        }
    }

    @Override
    public Tristate checkPermission(String permission) {
        assertInitialised();

        if (permission == null) {
            throw new NullPointerException("permission");
        }

        return checkPermission(permission, this.queryOptionsCache.getQueryOptions());
    }

    @Override
    public Tristate checkPermission(String permission, QueryOptions queryOptions) {
        assertInitialised();

        if (permission == null) {
            throw new NullPointerException("permission");
        }

        if (queryOptions == null) {
            throw new NullPointerException("queryOptions");
        }

        PermissionCache cache = this.user.getCachedData().getPermissionData(queryOptions);
        return cache.checkPermission(permission, CheckOrigin.PLATFORM_API_HAS_PERMISSION).result();
    }

    public User getUser() {
        assertInitialised();
        return this.user;
    }

    @Override
    public QueryOptions getQueryOptions() {
        return getQueryOptionsCache().getQueryOptions();
    }

    public QueryOptionsCache<ServerPlayerEntity> getQueryOptionsCache() {
        assertInitialised();
        return this.queryOptionsCache;
    }

    public Locale getLocale(ServerPlayerEntity player) {
        if (this.language == null || !this.language.equals(player.getLanguage())) {
            this.language = player.getLanguage();
            this.locale = TranslationManager.parseLocale(this.language);
        }

        return this.locale;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return INSTANCE.orEmpty(cap, handle);
    }

    private static class CapStorage implements Capability.IStorage<UserCapability> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<UserCapability> capability, UserCapability instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<UserCapability> capability, UserCapability instance, Direction side, INBT nbt) { }
    }
}