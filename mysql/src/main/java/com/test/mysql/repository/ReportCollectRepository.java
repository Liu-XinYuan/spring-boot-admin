package com.test.mysql.repository;



import com.test.mysql.entity.F_garbageCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportCollectRepository extends JpaRepository<F_garbageCollect, Long> {




    /**
     *
     * @param start 上传时间开始
     * @param end   上传时间结束
     * @param pageRequest
     * @return
     */
    @Query(value="select NEW com.test.mysql.entity.F_garbageCollect(e.categoryId ,e.categoryName,e.department ,sum(e.netWeight)) from f_garbage e " +
            "where e.up_Date >= :start and e.up_Date <= :end group by e.department,e.categoryId,e.categoryName ORDER BY e.department,e.categoryId asc ")
    Page<F_garbageCollect> findByTime(@Param("start") Date start, @Param("end") Date end, Pageable pageRequest);


    @Query(value="select NEW com.test.mysql.entity.F_garbageCollect(e.categoryId ,e.categoryName,e.department ,sum(e.netWeight)) from f_garbage e " +
            "where e.up_Date >= :start and e.up_Date <= :end group by e.department,e.categoryId,e.categoryName ORDER BY e.department,e.categoryId asc ")
    List<F_garbageCollect> findAll(@Param("start") Date start, @Param("end") Date end);
}