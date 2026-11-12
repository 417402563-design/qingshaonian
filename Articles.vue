<template>
  <ContentList api="/content/articles" title="🌱 成长驿站科普" subtitle="读懂情绪，读懂正在长大的自己"
               filter-param="category" :filters="['情绪', '人际', '家庭', '成长']" tag-key="category" />
</template>

<script setup>
import ContentList from '../../components/ContentList.vue'
</script>
