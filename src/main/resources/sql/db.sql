

insert into TBL_USER values (1, null, null, 'admin', '123', TRUE);


insert into TBL_OFFICE values (1, 'دفتر1', current_date(), null, null);
insert into TBL_OFFICE values (2, 'دفتر2', current_date(), null, null);
insert into TBL_OFFICE values (3, 'دفتر3', current_date(), null, null);
insert into TBL_OFFICE values (4, 'دفتر4', current_date(), null, null);


insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (1, '1', 'وجه نقد', 1, FALSE, TRUE, null);
insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (2, '2', 'آب شده', 1, FALSE, FALSE, null);
insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (3, '3', 'متفرقه', 1, FALSE, TRUE, null);
insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (4, '4', 'ساخته', 1, FALSE, TRUE, null);
insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (5, '5', 'ارز', 1, FALSE, TRUE, null);
insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (6, '6', 'سکه', 1, FALSE, TRUE, null);
insert into TBL_PRODUCT_CATEGORY (id, code, title, office_id, modifiable, countable, description) values (7, '7', 'سنگ', 1, FALSE, FALSE, null);



insert into TBL_PRODUCT (CODE, PRODUCTNAME, OFFICE_ID, PRODUCTCATEGORY_ID) values ('1', 'ریال', 1, 1);
insert into TBL_PRODUCT (CODE, PRODUCTNAME, OFFICE_ID, PRODUCTCATEGORY_ID) values ('2', 'طلا', 1, 2);


