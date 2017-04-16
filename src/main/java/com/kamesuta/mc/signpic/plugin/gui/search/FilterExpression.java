package com.kamesuta.mc.signpic.plugin.gui.search;

import java.util.Date;
import java.util.List;
import java.util.Queue;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.kamesuta.mc.signpic.plugin.SignData;
import com.kamesuta.mc.signpic.plugin.gui.search.DateFilterElement.DateFilterProperty;
import com.kamesuta.mc.signpic.plugin.gui.search.StringFilterElement.StringFilterProperty;

public class FilterExpression {

	protected final @Nonnull List<SignData> datas;
	protected final @Nonnull Queue<IFilterElement> elements = Queues.newArrayDeque();

	public FilterExpression(final @Nonnull List<SignData> datas) {
		this.datas = datas;
	}

	protected void add(final IFilterElement date) {
		this.elements.add(date);
	}

	/**
	 * Date equals
	 * @param property
	 * @param date
	 * @return this
	 */
	public FilterExpression deq(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.EqualsDateFilterElement(property, date));
		return this;
	}

	/**
	 * Date Hour equals
	 * @param property
	 * @param date
	 * @return
	 */
	public FilterExpression dheq(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.HourEqualsDateFilterElement(property, date));
		return this;
	}

	/**
	 * Date Day equals
	 * @param property
	 * @param date
	 * @return
	 */
	public FilterExpression ddeq(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.DayEqualsDateFilterElement(property, date));
		return this;
	}

	/**
	 * Date Month equals
	 * @param property
	 * @param date
	 * @return
	 */
	public FilterExpression dmeq(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.MonthEqualsDateFilterElement(property, date));
		return this;
	}

	/**
	 * Date year equals
	 * @param property
	 * @param date
	 * @return
	 */
	public FilterExpression dyeq(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.YearEqualsDateFilterElement(property, date));
		return this;
	}

	/**
	 * Date after
	 * @param property
	 * @param date
	 * @return
	 */
	public FilterExpression da(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.AfterDateFilterElement(property, date));
		return this;
	}

	/**
	 * Date before
	 * @param property
	 * @param date
	 * @return
	 */
	public FilterExpression db(final DateFilterProperty property, final Date date) {
		add(new DateFilterElement.BeforeDateFilterElement(property, date));
		return this;
	}

	/**
	 * String equals
	 * @param property
	 * @param str
	 * @return this
	 */
	public FilterExpression eq(final StringFilterProperty property, final String str) {
		add(new StringFilterElement.EqualsStringFilterElement(property, str));
		return this;
	}

	/**
	 * String equalsIgnoreCase
	 * @param property
	 * @param str
	 * @return this
	 */
	public FilterExpression ieq(final StringFilterProperty property, final String str) {
		add(new StringFilterElement.EqualsIgnoreCaseStringFilterElement(property, str));
		return this;
	}

	/**
	 * Block pos equals
	 * @param x
	 * @param y
	 * @param z
	 * @return this
	 */
	public FilterExpression peq(final int x, final int y, final int z) {
		add(new PosFilterElement.EqualPosFilterElement(x, y, z));
		return this;
	}

	/**
	 * Block pos within range
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return this
	 */
	public FilterExpression wp(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		add(new PosFilterElement.RangePosFilterElement.WithinRangePosFilterElement(x1, y1, z1, x2, y2, z2));
		return this;
	}

	/**
	 * Block pos out range
	 * @param x1
	 * @param y1
	 * @param z1
	 * @param x2
	 * @param y2
	 * @param z2
	 * @return
	 */
	public FilterExpression op(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		add(new PosFilterElement.RangePosFilterElement.OutRangePosFilterElement(x1, y1, z1, x2, y2, z2));
		return this;
	}

	public List<SignData> findList() {
		final List list = Lists.newArrayList();
		for (final SignData data : this.datas) {
			for (final IFilterElement element : this.elements) {
				if (element.filter(data)) {
					list.add(data);
					break;
				}
			}
		}
		return list;
	}
}