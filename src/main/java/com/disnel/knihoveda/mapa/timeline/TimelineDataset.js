
class TimelineDataset
{
	
	constructor(color, yearData, countData)
	{
		this.color = color;
		
		this.data = {};

		yearData.forEach(function(year, index)
		{
			this.data[year] = countData[index];
		}, this);
	}
	
}
