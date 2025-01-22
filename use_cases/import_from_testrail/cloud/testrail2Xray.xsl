<!-- testrail2Xray.xsl: Transform TestRail XML export to XRay CSV format -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0">
  <xsl:output method="text" indent="no"/>
  <xsl:strip-space elements="*"/>

  <!-- https://stackoverflow.com/questions/30859767/is-it-possible-to-preprocess-xml-source-within-the-same-xslt-stylesheet -->

  <xsl:template match="/">
    <!-- Enrich the input data for cross-referencing & easier ordering -->
    <xsl:variable name="enriched_buffer">
      <xsl:apply-templates mode="enricher"/>
    </xsl:variable>

    <!-- Process the enriched data against the transformation rules -->
    <xsl:apply-templates select="$enriched_buffer/*"/>
  </xsl:template>

  <!-- Replicate the input, and in passing: -->
  <!-- 1. Index elements of interest -->
  <!-- 2. Build paths of (test repo) hierarchies -->

  <xsl:template match="*" mode="enricher">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="index">
        <xsl:number from="/" level="any" count="preconds|steps|steps_separated|custom[mission or goals]|case[not(custom)]"/>
      </xsl:attribute>
      <xsl:if test="local-name(..) = 'section' and local-name() = 'name'">
        <xsl:attribute name="path">
          <xsl:for-each select="ancestor::section/name">
            <xsl:if test="position() > 1"><xsl:text>/</xsl:text></xsl:if>
            <xsl:value-of select="."/>
          </xsl:for-each>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates mode="enricher"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="put_detail">
    <xsl:param name="IssueID"               ></xsl:param>
    <xsl:param name="IssueKey"              ></xsl:param>
    <xsl:param name="TestType"              ></xsl:param>
    <xsl:param name="TestSummary"           ></xsl:param>
    <xsl:param name="TestPriority"          ></xsl:param>
    <xsl:param name="Action"                ></xsl:param>
    <xsl:param name="Data"                  ></xsl:param>
    <xsl:param name="Result"                ></xsl:param>
    <xsl:param name="TestRepo"              ></xsl:param>
    <xsl:param name="Precondition"          ></xsl:param>
    <xsl:param name="IssueType"             ></xsl:param>
    <xsl:param name="PreconditionType"      ></xsl:param>
    <xsl:param name="UnstructuredDefinition"></xsl:param>
    <xsl:param name="Labels"                ></xsl:param>

    <xsl:value-of select="$IssueID" /><xsl:text>,</xsl:text>
    <xsl:value-of select="$IssueKey"/><xsl:text>,</xsl:text>

    <xsl:choose>
      <xsl:when test="normalize-space($TestType) = 'Manual'">
        <xsl:value-of select="normalize-space($TestType)"/>
      </xsl:when>
      <xsl:when test="normalize-space($TestType) = 'Exploratory'">
        <xsl:value-of select="normalize-space($TestType)"/>
      </xsl:when>
      <xsl:when test="normalize-space($TestType) = 'Automated'"  >
        <xsl:text>Generic</xsl:text>
      </xsl:when>
      <xsl:when test="normalize-space($TestType) = 'precondition'">
        <xsl:text></xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Manual</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>,</xsl:text>

    <xsl:if test="contains($TestSummary, '&#10;')">"</xsl:if>
    <xsl:value-of select="$TestSummary"/>
    <xsl:if test="contains($TestSummary, '&#10;')">"</xsl:if>
    <xsl:text>,</xsl:text>

    <xsl:choose>
      <xsl:when test="normalize-space($TestPriority) = 'Critical'"    >1</xsl:when>
      <xsl:when test="normalize-space($TestPriority) = 'High'"        >2</xsl:when>
      <xsl:when test="normalize-space($TestPriority) = 'Medium'"      >3</xsl:when>
      <xsl:when test="normalize-space($TestPriority) = 'Low'"         >4</xsl:when>
      <xsl:when test="normalize-space($TestPriority) = 'precondition'"></xsl:when>
      <xsl:otherwise                                                  >3</xsl:otherwise>
    </xsl:choose>
    <xsl:text>,</xsl:text>

    <xsl:if test="contains($Action, '&#10;')">"</xsl:if>
    <xsl:value-of select="$Action"/>
    <xsl:if test="contains($Action, '&#10;')">"</xsl:if>
    <xsl:text>,</xsl:text>

    <xsl:if test="contains($Data, '&#10;')">"</xsl:if>
    <xsl:value-of select="$Data"/>
    <xsl:if test="contains($Data, '&#10;')">"</xsl:if>
    <xsl:text>,</xsl:text>

    <xsl:if test="contains($Result, '&#10;')">"</xsl:if>
    <xsl:value-of select="$Result"/>
    <xsl:if test="contains($Result, '&#10;')">"</xsl:if>
    <xsl:text>,</xsl:text>

    <xsl:value-of select="$TestRepo"/><xsl:text>,</xsl:text>

    <xsl:if test="contains($Precondition, '&#10;')">"</xsl:if>
    <xsl:value-of select="$Precondition"/>
    <xsl:if test="contains($Precondition, '&#10;')">"</xsl:if>
    <xsl:text>,</xsl:text>

    <xsl:value-of select="$IssueType"/><xsl:text>,</xsl:text>
    <xsl:value-of select="$PreconditionType"/><xsl:text>,</xsl:text>

    <xsl:if test="contains($UnstructuredDefinition, '&#10;')">"</xsl:if>
    <xsl:value-of select="$UnstructuredDefinition"/>
    <xsl:if test="contains($UnstructuredDefinition, '&#10;')">"</xsl:if>
    <xsl:text>,</xsl:text>

    <xsl:value-of select="$Labels"/><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="/suite">
    <xsl:text>Issue ID,</xsl:text>
    <xsl:text>Issue Key,</xsl:text>
    <xsl:text>Test Type,</xsl:text>
    <xsl:text>Test Summary,</xsl:text>
    <xsl:text>Test Priority,</xsl:text>
    <xsl:text>Action,</xsl:text>
    <xsl:text>Data,</xsl:text>
    <xsl:text>Result,</xsl:text>
    <xsl:text>Test Repo,</xsl:text>
    <xsl:text>Precondition,</xsl:text>
    <xsl:text>Issue Type,</xsl:text>
    <xsl:text>Precondition Type,</xsl:text>
    <xsl:text>Unstructured Definition,</xsl:text>
    <xsl:text>Labels</xsl:text>
    <xsl:text>
</xsl:text>
    <xsl:apply-templates select="/suite//section/cases/case/custom/node()[local-name()='preconds' or local-name()='steps' or local-name()='steps_separated']|/suite//section/cases/case/custom[mission or goals]|/suite//section/cases/case">
      <xsl:sort select="number(@index)" stable="yes"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/suite//section/cases/case/custom/preconds">
    <xsl:call-template name="put_detail">
      <xsl:with-param name="IssueID"          select="@index"/>
      <xsl:with-param name="TestType"         select="'precondition'"/>
      <xsl:with-param name="TestSummary"      select="."/>
      <xsl:with-param name="TestPriority"     select="'precondition'"/>
      <xsl:with-param name="IssueType"        select="'precondition'"/>
      <xsl:with-param name="PreconditionType" select="'Manual'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="/suite//section/cases/case/custom/steps">
    <xsl:call-template name="put_detail">
      <xsl:with-param name="IssueID"      select="@index"/>
      <xsl:with-param name="TestType"     select="../automation_type/value"/>
      <xsl:with-param name="TestSummary"  select="../../title"/>
      <xsl:with-param name="TestPriority" select="../../priority"/>
      <xsl:with-param name="Action"       select="."/>
      <xsl:with-param name="Result"       select="../expected"/>
      <xsl:with-param name="TestRepo"     select="ancestor::section[1]/name/@path"/>
      <xsl:with-param name="Precondition" select="../preconds/@index"/>
      <xsl:with-param name="Labels"       select="../../type"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/suite//section/cases/case/custom/steps_separated">
    <xsl:for-each select="step">
      <xsl:if test="position() = 1">
        <xsl:call-template name="put_detail">
          <xsl:with-param name="IssueID"      select="../@index"/>
          <xsl:with-param name="TestType"     select="../../automation_type/value"/>
          <xsl:with-param name="TestSummary"  select="../../../title"/>
          <xsl:with-param name="TestPriority" select="../../../priority"/>
          <xsl:with-param name="Action"       select="content"/>
          <xsl:with-param name="Data"         select="additional_info"/>
          <xsl:with-param name="Result"       select="expected"/>
          <xsl:with-param name="TestRepo"     select="ancestor::section[1]/name/@path"/>
          <xsl:with-param name="Precondition" select="../../preconds/@index"/>
          <xsl:with-param name="Labels"       select="../../../type"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:if test="position() > 1">
        <xsl:call-template name="put_detail">
          <xsl:with-param name="IssueID"      select="../@index"/>
          <xsl:with-param name="TestType"     select="../../automation_type/value"/>
          <xsl:with-param name="Action"       select="content"/>
          <xsl:with-param name="Data"         select="additional_info"/>
          <xsl:with-param name="Result"       select="expected"/>
        </xsl:call-template>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="not(step)">
      <xsl:call-template name="put_detail">
        <xsl:with-param name="IssueID"      select="@index"/>
        <xsl:with-param name="TestType"     select="../automation_type/value"/>
        <xsl:with-param name="TestSummary"  select="../title"/>
        <xsl:with-param name="TestPriority" select="../priority"/>
        <xsl:with-param name="TestRepo"     select="ancestor::section[1]/name/@path"/>
        <xsl:with-param name="Precondition" select="../preconds/@index"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="/suite//section/cases/case/custom[mission or goals]">
    <xsl:call-template name="put_detail">
      <xsl:with-param name="IssueID"      select="@index"/>
      <xsl:with-param name="TestType"     select="'Exploratory'"/>
      <xsl:with-param name="TestSummary"  select="../title"/>
      <xsl:with-param name="TestPriority" select="../priority"/>
      <xsl:with-param name="TestRepo"     select="ancestor::section[1]/name/@path"/>
      <xsl:with-param name="Precondition" select="preconds/@index"/>
      <xsl:with-param name="UnstructuredDefinition">
        <xsl:choose>
          <xsl:when test="mission and goals">
            <xsl:text>*Mission:* </xsl:text>
            <xsl:value-of select="mission"/><xsl:text>
</xsl:text>
            <xsl:text> *Goals:* </xsl:text>
            <xsl:value-of select="goals"/><xsl:text>
</xsl:text>
          </xsl:when>
          <xsl:when test="mission">
            <xsl:text>"*Mission:* </xsl:text>
            <xsl:value-of select="mission"/><xsl:text>
</xsl:text>
          </xsl:when>
          <xsl:when test="goals">
            <xsl:text>"*Goals:* </xsl:text>
            <xsl:value-of select="goals"/><xsl:text>
</xsl:text>
          </xsl:when>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="Labels" select="../type"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="/suite//section/cases/case">
    <xsl:if test="not(custom)">
      <xsl:call-template name="put_detail">
        <xsl:with-param name="IssueID"      select="@index"/>
        <xsl:with-param name="TestType"     select="type"/>
        <xsl:with-param name="TestSummary"  select="title"/>
        <xsl:with-param name="TestPriority" select="priority"/>
        <xsl:with-param name="TestRepo"     select="ancestor::section[1]/name/@path"/>
        <xsl:with-param name="Labels"       select="type"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
