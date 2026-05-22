package com.example.flight_booking_app.data.model;

public class FareRule {
    private String fareRuleId;
    private String airlineId;      // Thuộc về hãng nào (VJ, VNA...)
    private String fareClassName;  // Tên hạng: Eco, Deluxe, SkyBoss, Business...

    // Quy định về hành lý
    private int cabinBaggage;      // Hành lý xách tay (thường là 7kg hoặc 12kg)
    private int checkedBaggage;    // Hành lý ký gửi miễn phí (0kg, 20kg, 30kg...)

    // Quy định về quyền lợi (Dùng để hiển thị trong chi tiết vé)
    private boolean isChangeable;     // Có được đổi ngày/giờ bay không?
    private boolean isRefundable;     // Có được hoàn tiền không?
    private boolean hasLoungeAccess;  // Có phòng chờ VIP không?
    private boolean hasPriority;      // Có ưu tiên check-in/lên tàu không?
    private boolean hasMeal;          // Có bao gồm suất ăn không?

    public FareRule() {}

    public FareRule(String fareRuleId, String airlineId, String fareClassName, int cabinBaggage, int checkedBaggage, boolean isChangeable, boolean isRefundable, boolean hasLoungeAccess, boolean hasPriority, boolean hasMeal) {
        this.fareRuleId = fareRuleId;
        this.airlineId = airlineId;
        this.fareClassName = fareClassName;
        this.cabinBaggage = cabinBaggage;
        this.checkedBaggage = checkedBaggage;
        this.isChangeable = isChangeable;
        this.isRefundable = isRefundable;
        this.hasLoungeAccess = hasLoungeAccess;
        this.hasPriority = hasPriority;
        this.hasMeal = hasMeal;
    }

    public String getFareRuleId() {
        return fareRuleId;
    }

    public void setFareRuleId(String fareRuleId) {
        this.fareRuleId = fareRuleId;
    }

    public boolean isHasLoungeAccess() {
        return hasLoungeAccess;
    }

    public void setHasLoungeAccess(boolean hasLoungeAccess) {
        this.hasLoungeAccess = hasLoungeAccess;
    }

    public String getAirlineId() { return airlineId; }
    public void setAirlineId(String airlineId) { this.airlineId = airlineId; }

    public String getFareClassName() { return fareClassName; }
    public void setFareClassName(String fareClassName) { this.fareClassName = fareClassName; }

    public int getCabinBaggage() { return cabinBaggage; }
    public void setCabinBaggage(int cabinBaggage) { this.cabinBaggage = cabinBaggage; }

    public int getCheckedBaggage() { return checkedBaggage; }
    public void setCheckedBaggage(int checkedBaggage) { this.checkedBaggage = checkedBaggage; }

    public boolean isChangeable() { return isChangeable; }
    public void setChangeable(boolean changeable) { isChangeable = changeable; }

    public boolean isRefundable() { return isRefundable; }
    public void setRefundable(boolean refundable) { isRefundable = refundable; }

    public boolean hasLoungeAccess() { return hasLoungeAccess; }
    public void setLoungeAccess(boolean loungeAccess) { hasLoungeAccess = loungeAccess; }

    public boolean isHasPriority() { return hasPriority; }
    public void setHasPriority(boolean hasPriority) { this.hasPriority = hasPriority; }

    public boolean isHasMeal() { return hasMeal; }
    public void setHasMeal(boolean hasMeal) { this.hasMeal = hasMeal; }
}
