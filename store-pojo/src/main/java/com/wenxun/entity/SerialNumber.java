package com.wenxun.entity;

import java.io.Serializable;

/**
 *
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table serial_number
 */
public class SerialNumber implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column serial_number.name
     *
     * @mbg.generated
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column serial_number.value
     *
     * @mbg.generated
     */
    private Integer value;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column serial_number.step
     *
     * @mbg.generated
     */
    private Integer step;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table serial_number
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column serial_number.name
     *
     * @return the value of serial_number.name
     *
     * @mbg.generated
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column serial_number.name
     *
     * @param name the value for serial_number.name
     *
     * @mbg.generated
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column serial_number.value
     *
     * @return the value of serial_number.value
     *
     * @mbg.generated
     */
    public Integer getValue() {
        return value;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column serial_number.value
     *
     * @param value the value for serial_number.value
     *
     * @mbg.generated
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column serial_number.step
     *
     * @return the value of serial_number.step
     *
     * @mbg.generated
     */
    public Integer getStep() {
        return step;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column serial_number.step
     *
     * @param step the value for serial_number.step
     *
     * @mbg.generated
     */
    public void setStep(Integer step) {
        this.step = step;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table serial_number
     *
     * @mbg.generated
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", name=").append(name);
        sb.append(", value=").append(value);
        sb.append(", step=").append(step);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}