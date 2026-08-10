-- Money must not be stored as a binary float: double cannot represent most
-- decimal amounts exactly, so sums and comparisons drift. The entity already
-- maps the column to BigDecimal.
alter table product alter column price set data type decimal(19, 2);
