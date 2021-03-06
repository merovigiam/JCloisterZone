package com.jcloisterzone.figure;

import com.google.common.base.Objects;
import com.jcloisterzone.Player;
import com.jcloisterzone.board.Location;
import com.jcloisterzone.board.Tile;
import com.jcloisterzone.board.pointer.FeaturePointer;
import com.jcloisterzone.event.MeepleEvent;
import com.jcloisterzone.feature.Feature;
import com.jcloisterzone.game.Game;

public abstract class Meeple extends Figure {

    private static final long serialVersionUID = 251811435063355665L;

    private transient final Player player;
    private transient Feature feature;
    private transient Integer index; //index distinguish meeples on same feature
    private Location location;

    public static class DeploymentCheckResult {
        public final boolean result;
        public final String error;

        private DeploymentCheckResult() {
            this.result = true;
            this.error = null;
        }

        public DeploymentCheckResult(String error) {
            this.result = false;
            this.error = error;
        }

        public static final DeploymentCheckResult OK = new DeploymentCheckResult();
    }

    public Meeple(Game game, Player player) {
        super(game);
        this.player = player;
    }

    public boolean canBeEatenByDragon() {
        return true;
    }

    /** true if meeple is deploayed on board */
    public boolean isDeployed() {
        return location != null && location != Location.PRISON ;
    }

    public boolean isInSupply() {
        return location == null;
    }

    public void clearDeployment() {
        setPosition(null);
        setLocation(null);
        setFeature(null);
    }

    public DeploymentCheckResult isDeploymentAllowed(Feature feature) {
        return DeploymentCheckResult.OK;
    }

    public Feature getDeploymentFeature(Tile tile, Location loc) {
        return tile.getFeature(loc);
    }

    public void deployUnoccupied(Tile tile, Location loc) {
        //perorm unoccupied check for followers only!!!
        Feature feature = getDeploymentFeature(tile, loc);
        deploy(tile, loc, feature);
    }

    public final void deploy(Tile tile, Location loc) {
        Feature feature = getDeploymentFeature(tile, loc);
        deploy(tile, loc, feature);
    }

    protected void deploy(Tile tile, Location loc, Feature feature) {
        DeploymentCheckResult check = isDeploymentAllowed(feature);
        if (!check.result) {
            throw new IllegalArgumentException(check.error);
        }
        feature.addMeeple(this);
        setPosition(tile.getPosition());
        setLocation(loc);
        setFeature(feature);
        game.post(new MeepleEvent(game.getActivePlayer(), this, null, new FeaturePointer(tile.getPosition(), loc)));
    }

    public final void undeploy() {
        undeploy(true);
    }

    public void undeploy(boolean checkForLonelyBuilderOrPig) {
        assert location != null && location != Location.PRISON;
        FeaturePointer source = new FeaturePointer(getPosition(), location);
        feature.removeMeeple(this);
        clearDeployment();
        game.post(new MeepleEvent(game.getActivePlayer(), this, source, null));
    }


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature piece) {
        this.feature = piece;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(index, location);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false; //compares exact types
        Meeple o = (Meeple) obj;
        if (!Objects.equal(player, o.player)) return false;
        if (!Objects.equal(index, o.index)) return false;
        if (!Objects.equal(location, o.location)) return false;
        //do not compare feature - location is enough - feature is changing during time
        return true;
    }

    @Override
    public String toString() {
        if (location == Location.PRISON) {
            return getClass().getSimpleName() + "(" + player.getIndex() + "," + location.toString() + ")";
        } else {
            return super.toString() + "(" + player.getIndex() + ")";
        }
    }

}
