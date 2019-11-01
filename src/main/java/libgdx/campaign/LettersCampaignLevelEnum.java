package libgdx.campaign;

public enum LettersCampaignLevelEnum implements CampaignLevel {

    //This order is displayed on the campaign screen
    ;

    @Override
    public int getIndex() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }

}
